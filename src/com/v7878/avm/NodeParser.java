package com.v7878.avm;

import static com.v7878.avm.Constants.NODE_PRIVATE;
import static com.v7878.avm.Constants.NODE_PROTECTED;
import com.v7878.avm.bytecode.Init;
import com.v7878.avm.bytecode.Instruction;
import com.v7878.avm.exceptions.ParseException;
import com.v7878.avm.exceptions.ParseTokenException;
import com.v7878.avm.utils.DualBuffer;
import com.v7878.avm.utils.NewApiUtils;
import com.v7878.avm.utils.Pair;
import com.v7878.avm.utils.Token;
import com.v7878.avm.utils.Tokenizer;
import com.v7878.avm.utils.Wide;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NodeParser {

    public static final String DIGITS = "0123456789abcdef";
    public static final String SIMPLE_UINT = "(0|[1-9]\\d*)";
    public static final String INT32 = "-?" + SIMPLE_UINT;
    public static final String OCTAL_INT32 = "-?0[0-7]+";
    public static final String BINARY_INT32 = "-?0[bB][01]+";
    public static final String HEX_INT32 = "-?0[xX][0-9a-fA-F]+";
    public static final String ALL_INT_NO_POSTFIX = "(" + INT32 + "|" + OCTAL_INT32 + "|" + BINARY_INT32 + "|" + HEX_INT32 + ")";
    public static final String INT_POSTFIXES = "[zZsSlLwW]";
    public static final String ALL_INT_WITH_POSTFIX = ALL_INT_NO_POSTFIX + INT_POSTFIXES;
    //public static final String ALL_INT = ALL_INT_NO_POSTFIX + INT_POSTFIXES + "?";
    public static final String STRING = "\"( |\\\\u[0-9a-fA-F]{4}|\\\\t|\\\\b|\\\\n|\\\\f|\\\\r|\\\\\"|\\\\'|\\\\\\\\|[^\"\\s\\\\])*\"";
    public static final String REGISTER = "[dDvVpPrR]" + SIMPLE_UINT;
    public static final String IDENTIFIER = ":\\w+";
    public static final String INSTRUCTION_NAME = "\\w(-?\\w+)*";
    public static final String MODIFIER_PRIVATE = "private"; // call only
    public static final String MODIFIER_PROTECTED = "protected"; //read and call only

    private static final Map<String, InstructionCreator> icreator = new HashMap<>();

    static {
        Init.init();
    }

    public static void addCreator(String iname, InstructionCreator ict) {
        if (!Objects.requireNonNull(iname).matches(INSTRUCTION_NAME)) {
            throw new IllegalArgumentException();
        }
        if (NewApiUtils.putIfAbsent(icreator, iname, Objects.requireNonNull(ict)) != null) {
            throw new IllegalArgumentException();
        }
    }

    public static Node[] parseNodes(String data) throws ParseException {
        Token[] tokens = new Tokenizer(data).parseTokens();
        List<Token[]> nodes = new ArrayList<>();
        int start = -1, end = -1;
        for (int i = 0; i < tokens.length; i++) {
            switch (tokens[i].data) {
                case ".node":
                    if (start == -1) {
                        start = i;
                    } else {
                        //TODO message
                        throw new ParseTokenException(tokens[i]);
                    }
                    break;
                case ".endnode":
                    if (start != -1) {
                        Token[] node = new Token[i - start];
                        System.arraycopy(tokens, start, node, 0, node.length);
                        nodes.add(node);
                        start = -1;
                        end = i;
                    } else {
                        //TODO message
                        throw new ParseTokenException(tokens[i]);
                    }
                    break;
                default:
                    if (i == 0) {
                        throw new ParseTokenException(tokens[i]);
                    }
                    break;
            }
        }
        if (start != -1) {
            throw new ParseTokenException(tokens[start], "unclosed node block ");
        }
        if (end != tokens.length - 1) {
            throw new ParseTokenException(tokens[end + 1]);
        }
        Node[] out = new Node[nodes.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = parseNode(nodes.get(i));
        }
        return out;
    }

    private static Node parseNode(Token[] tokens) throws ParseException {
        if (tokens.length == 0) {
            throw new IllegalArgumentException();
        }
        if (tokens.length == 1) {
            throw new ParseTokenException(tokens[0], "not contains name ");
        }
        int i = 1;
        int flags = 0;
        switch (tokens[i].data) {
            case MODIFIER_PRIVATE:
                i++;
                flags |= NODE_PRIVATE | NODE_PROTECTED;
                break;
            case MODIFIER_PROTECTED:
                i++;
                flags |= NODE_PROTECTED;
                break;
        }
        if (i == tokens.length) {
            throw new ParseTokenException(tokens[0], "not contains name ");
        }
        if (!tokens[i].data.matches(STRING)) {
            throw new ParseTokenException(tokens[i], "not a string ");
        }
        String name = (String) ParamType.String.parse(tokens[i]);
        i++;
        if (i == tokens.length) {
            return Machine.get().newNode(flags, 0);
        }
        ByteBuffer data = null;
        Node out = null;
        ret:
        {
            if (tokens[i].data.equals(".data")) {
                int start = i;
                boolean end = false;
                for (; i < tokens.length; i++) {
                    if (tokens[i].data.equals(".enddata")) {
                        end = true;
                        break;
                    }
                }
                if (!end) {
                    throw new ParseTokenException(tokens[start], "unclosed data block ");
                }
                data = parseData(Arrays.copyOfRange(tokens, start, i));
                i++;
                if (i == tokens.length) {
                    out = Machine.get().newNode(flags, data, false);
                    break ret;
                }
            }
            if (tokens[i].data.equals(".code")) {
                int start = i;
                int end = -1;
                for (; i < tokens.length; i++) {
                    if (tokens[i].data.equals(".endcode")) {
                        end = i;
                        break;
                    }
                }
                if (end == -1) {
                    throw new ParseTokenException(tokens[start], "unclosed code block ");
                }
                if (end != tokens.length - 1) {
                    throw new ParseTokenException(tokens[end + 1]);
                }
                Object[] code = parseCode(Arrays.copyOfRange(tokens, start, end));
                Instruction[] instrs = (Instruction[]) code[0];
                int vregs = (int) code[1], ins = (int) code[2], outs = (int) code[3];
                if (data == null) {
                    out = Machine.get().newNode(flags, instrs, vregs, ins, outs);
                    break ret;
                }
                out = Machine.get().newNode(flags, data, false, instrs, vregs, ins, outs);
                break ret;
            } else {
                throw new ParseTokenException(tokens[i]);
            }
        }
        Machine.get().setNodeName(out, name);
        return out;
    }

    private static ByteBuffer parseData(Token[] tokens) throws ParseException {
        if (tokens.length == 0) {
            throw new IllegalArgumentException();
        }
        if (tokens.length < 2) {
            throw new ParseTokenException(tokens[0], "not contains all params ");
        }
        int i = 1;
        int size = (int) ParamType.SimpleUInt.parse(tokens[i++]);
        ByteBuffer out = Machine.allocate(size);
        for (; i < tokens.length; i++) {
            String data = tokens[i].data;
            if (data.matches(ALL_INT_NO_POSTFIX)) {
                out.putInt((int) ParamType.Int32.parse(tokens[i]));
            } else if (data.matches(ALL_INT_WITH_POSTFIX)) {
                switch (data.charAt(data.length() - 1)) {
                    case 'Z':
                    case 'z':
                        out.put((byte) ParamType.Int8.parse(tokens[i]));
                        break;
                    case 'S':
                    case 's':
                        out.putShort((short) ParamType.Int16.parse(tokens[i]));
                        break;
                    case 'L':
                    case 'l':
                        out.putLong((long) ParamType.Int64.parse(tokens[i]));
                        break;
                    case 'W':
                    case 'w':
                        DualBuffer.putWide(out, (Wide) ParamType.Int128.parse(tokens[i]));
                    default:
                        throw new IllegalStateException();
                }
            } else {
                throw new ParseTokenException(tokens[i], "unknown data type ");
            }
        }
        return out;
    }

    private static Object[] parseCode(Token[] tokens) throws ParseException {
        if (tokens.length == 0) {
            throw new IllegalArgumentException();
        }
        if (tokens.length < 4) {
            throw new ParseTokenException(tokens[0], "not contains all params ");
        }
        int i = 1;
        Object[] out = new Object[4];
        int vregs = (int) (out[1] = ParamType.SimpleUInt.parse(tokens[i++]));
        int ins = (int) (out[2] = ParamType.SimpleUInt.parse(tokens[i++]));
        int outs = (int) (out[3] = ParamType.SimpleUInt.parse(tokens[i++]));
        List<Pair<InstructionCreator, Token[]>> creators = new ArrayList<>();
        Map<String, Integer> indentifiers = new HashMap<>();
        for (int t = 0; i < tokens.length; t++) {
            if (tokens[i].data.matches(IDENTIFIER)) {
                indentifiers.put(tokens[i].data, t);
                i++;
            }
            String iname = tokens[i].data;
            if (!iname.matches(INSTRUCTION_NAME)) {
                throw new ParseTokenException(tokens[i], "not an instruction name ");
            }
            InstructionCreator ict = icreator.get(iname);
            if (ict == null) {
                throw new ParseTokenException(tokens[i], "unknown instruction ");
            }
            Token[] iparams = new Token[ict.getParamsCount()];
            if (tokens.length - i <= iparams.length) {
                throw new ParseTokenException(tokens[i], "not contains all params ");
            }
            i++;
            //TODO copy
            for (int g = 0; g < iparams.length; g++, i++) {
                iparams[g] = tokens[i];
            }
            creators.add(new Pair<>(ict, iparams));
        }
        Instruction[] instructions = new Instruction[creators.size()];
        for (i = 0; i < instructions.length; i++) {
            Pair<InstructionCreator, Token[]> creator = creators.get(i);
            instructions[i] = creator.a.getInstruction(creator.b, vregs, ins, outs, i, indentifiers);
        }
        out[0] = instructions;
        return out;
    }

    public enum ParamType {
        Register() {
            @Override
            public Integer parse(Token token, Object... params2) throws ParseTokenException {
                String iparam = token.data;
                if (!iparam.matches(REGISTER)) {
                    throw new ParseTokenException(token, "not a register ");
                }
                int vregs = (int) params2[0], ins = (int) params2[1], outs = (int) params2[2];
                iparam = iparam.toLowerCase();
                char type = iparam.charAt(0);
                int reg;
                try {
                    reg = NewApiUtils.parseInt(iparam, 1, iparam.length(), 10);
                } catch (NumberFormatException e) {
                    throw new ParseTokenException(token);
                }
                switch (type) {
                    case 'v':
                        return reg;
                    case 'p':
                        return vregs - outs - ins + reg;
                    case 'r':
                        return vregs - outs + reg;
                    case 'd':
                        return -reg - 1;
                    default:
                        throw new ParseTokenException(token, "unknown register type ");
                }
            }

            @Override
            public Integer getDefault() {
                return 0;
            }
        }, Identifier() {
            @Override
            public Integer parse(Token token, Object... params2) throws ParseTokenException {
                String iparam = token.data;
                if (!iparam.matches(IDENTIFIER)) {
                    throw new ParseTokenException(token, "not an identifier ");
                }
                @SuppressWarnings("unchecked")
                HashMap<String, Integer> lines = (HashMap<String, Integer>) params2[0];
                int i = (int) params2[1];
                return lines.get(iparam) - i;
            }

            @Override
            public Integer getDefault() {
                return 0;
            }
        }, SimpleUInt() {
            @Override
            public Integer parse(Token token, Object... params2) throws ParseTokenException {
                String iparam = token.data;
                if (!iparam.matches(SIMPLE_UINT)) {
                    throw new ParseTokenException(token, "not a simple uint ");
                }
                int out;
                try {
                    out = Integer.parseInt(iparam);
                } catch (NumberFormatException e) {
                    throw new ParseTokenException(token);
                }
                return out;
            }

            @Override
            public Integer getDefault() {
                return 0;
            }
        }, Int8() {
            @Override
            public Byte parse(Token token, Object... params2) throws ParseTokenException {
                String iparam = token.data;
                boolean negative = iparam.charAt(0) == '-';
                if (negative) {
                    iparam = iparam.substring(1);
                }
                byte m, out = 0;
                if (iparam.matches(INT32 + "[zZ]")) {
                    iparam = iparam.substring(0, iparam.length() - 1);
                    m = 10;
                } else if (iparam.matches(HEX_INT32 + "[zZ]")) {
                    iparam = iparam.substring(2, iparam.length() - 1);
                    m = 16;
                } else if (iparam.matches(BINARY_INT32 + "[zZ]")) {
                    iparam = iparam.substring(2, iparam.length() - 1);
                    m = 2;
                } else if (iparam.matches(OCTAL_INT32 + "[zZ]")) {
                    iparam = iparam.substring(1, iparam.length() - 1);
                    m = 8;
                } else {
                    throw new ParseTokenException(token, "not an int8 ");
                }
                iparam = iparam.toLowerCase();
                for (int i = 0; i < iparam.length(); i++) {
                    out *= m;
                    out += DIGITS.indexOf(iparam.charAt(i));
                }
                if (negative) {
                    out *= -1;
                }
                return out;
            }

            @Override
            public Byte getDefault() {
                return 0;
            }
        }, Int16() {
            @Override
            public Short parse(Token token, Object... params2) throws ParseTokenException {
                String iparam = token.data;
                boolean negative = iparam.charAt(0) == '-';
                if (negative) {
                    iparam = iparam.substring(1);
                }
                short m, out = 0;
                if (iparam.matches(INT32 + "[sS]")) {
                    iparam = iparam.substring(0, iparam.length() - 1);
                    m = 10;
                } else if (iparam.matches(HEX_INT32 + "[sS]")) {
                    iparam = iparam.substring(2, iparam.length() - 1);
                    m = 16;
                } else if (iparam.matches(BINARY_INT32 + "[sS]")) {
                    iparam = iparam.substring(2, iparam.length() - 1);
                    m = 2;
                } else if (iparam.matches(OCTAL_INT32 + "[sS]")) {
                    iparam = iparam.substring(1, iparam.length() - 1);
                    m = 8;
                } else {
                    throw new ParseTokenException(token, "not an int16 ");
                }
                iparam = iparam.toLowerCase();
                for (int i = 0; i < iparam.length(); i++) {
                    out *= m;
                    out += DIGITS.indexOf(iparam.charAt(i));
                }
                if (negative) {
                    out *= -1;
                }
                return out;
            }

            @Override
            public Short getDefault() {
                return 0;
            }
        }, Int32() {
            @Override
            public Integer parse(Token token, Object... params2) throws ParseTokenException {
                String iparam = token.data;
                boolean negative = iparam.charAt(0) == '-';
                if (negative) {
                    iparam = iparam.substring(1);
                }
                int m, out = 0;
                if (iparam.matches(INT32)) {
                    m = 10;
                } else if (iparam.matches(HEX_INT32)) {
                    iparam = iparam.substring(2);
                    m = 16;
                } else if (iparam.matches(BINARY_INT32)) {
                    iparam = iparam.substring(2);
                    m = 2;
                } else if (iparam.matches(OCTAL_INT32)) {
                    iparam = iparam.substring(1);
                    m = 8;
                } else {
                    throw new ParseTokenException(token, "not an int32 ");
                }
                iparam = iparam.toLowerCase();
                for (int i = 0; i < iparam.length(); i++) {
                    out *= m;
                    out += DIGITS.indexOf(iparam.charAt(i));
                }
                return negative ? -out : out;
            }

            @Override
            public Integer getDefault() {
                return 0;
            }
        }, Int64() {
            @Override
            public Long parse(Token token, Object... params2) throws ParseTokenException {
                String iparam = token.data;
                boolean negative = iparam.charAt(0) == '-';
                if (negative) {
                    iparam = iparam.substring(1);
                }
                long m, out = 0;
                if (iparam.matches(INT32 + "[lL]")) {
                    iparam = iparam.substring(0, iparam.length() - 1);
                    m = 10;
                } else if (iparam.matches(HEX_INT32 + "[lL]")) {
                    iparam = iparam.substring(2, iparam.length() - 1);
                    m = 16;
                } else if (iparam.matches(BINARY_INT32 + "[lL]")) {
                    iparam = iparam.substring(2, iparam.length() - 1);
                    m = 2;
                } else if (iparam.matches(OCTAL_INT32 + "[lL]")) {
                    iparam = iparam.substring(1, iparam.length() - 1);
                    m = 8;
                } else {
                    throw new ParseTokenException(token, "not an int64 ");
                }
                iparam = iparam.toLowerCase();
                for (int i = 0; i < iparam.length(); i++) {
                    out *= m;
                    out += DIGITS.indexOf(iparam.charAt(i));
                }
                return negative ? -out : out;
            }

            @Override
            public Long getDefault() {
                return 0L;
            }
        }, Int128() {
            @Override
            public Wide parse(Token token, Object... params2) throws ParseTokenException {
                String iparam = token.data;
                boolean negative = iparam.charAt(0) == '-';
                if (negative) {
                    iparam = iparam.substring(1);
                }
                Wide m, out = Wide.ZERO;
                if (iparam.matches(INT32 + "[wW]")) {
                    iparam = iparam.substring(0, iparam.length() - 1);
                    m = Wide.valueOf(10);
                } else if (iparam.matches(HEX_INT32 + "[wW]")) {
                    iparam = iparam.substring(2, iparam.length() - 1);
                    m = Wide.valueOf(16);
                } else if (iparam.matches(BINARY_INT32 + "[wW]")) {
                    iparam = iparam.substring(2, iparam.length() - 1);
                    m = Wide.valueOf(2);
                } else if (iparam.matches(OCTAL_INT32 + "[wW]")) {
                    iparam = iparam.substring(1, iparam.length() - 1);
                    m = Wide.valueOf(8);
                } else {
                    throw new ParseTokenException(token, "not an int128 ");
                }
                iparam = iparam.toLowerCase();
                for (int i = 0; i < iparam.length(); i++) {
                    out = out.multiply(m);
                    out = out.add(Wide.valueOf(DIGITS.indexOf(iparam.charAt(i))));
                }
                return negative ? out.negate() : out;
            }

            @Override
            public Wide getDefault() {
                return Wide.ZERO;
            }
        }, String() {
            @Override
            public String parse(Token token, Object... params2) throws ParseTokenException {
                String iparam = token.data;
                if (!iparam.matches(STRING)) {
                    throw new ParseTokenException(token, "not a string ");
                }
                iparam = iparam.substring(1, iparam.length() - 1);
                StringBuilder out = new StringBuilder();
                for (int i = 0; i < iparam.length();) {
                    char c = iparam.charAt(i);
                    if (c == '\\') {
                        char c2 = iparam.charAt(i + 1);
                        if (c2 == 'u') {
                            String ss = iparam.substring(i + 2, i + 6);
                            //TODO exception
                            out.append((char) Integer.parseInt(ss, 16));
                            i += 6;
                        } else {
                            switch (c2) {
                                case '\\':
                                    out.append('\\');
                                    break;
                                case 't':
                                    out.append('\t');
                                    break;
                                case 'b':
                                    out.append('\b');
                                    break;
                                case 'n':
                                    out.append('\n');
                                    break;
                                case 'f':
                                    out.append('\f');
                                    break;
                                case 'r':
                                    out.append('\r');
                                    break;
                                case '"':
                                    out.append('\"');
                                    break;
                                case '\'':
                                    out.append('\'');
                                    break;
                                default:
                                    throw new ParseTokenException(token, "illegal char '" + c2 + "' ");
                            }
                            i += 2;
                        }
                    } else {
                        out.append(c);
                        i++;
                    }
                }
                return out.toString();
            }

            @Override
            public String getDefault() {
                return "";
            }
        };

        public abstract Object parse(Token token, Object... params2) throws ParseException;

        public abstract Object getDefault();
    }

    public interface InstructionCreator {

        Instruction getInstruction(Token[] tokens,
                int vregs, int ins, int outs, int inum,
                Map<String, Integer> indentifiers) throws ParseException;

        int getParamsCount();
    }

    @FunctionalInterface
    public interface InstructionCreator2 {

        Instruction getInstruction(Object[] iparams);
    }

    public static class SimpleInstructionCreator implements InstructionCreator {

        private final InstructionCreator2 ict;
        private final ParamType[] paramtypes;

        public SimpleInstructionCreator(InstructionCreator2 ict, ParamType... paramtypes) {
            this.ict = Objects.requireNonNull(ict);
            this.paramtypes = Objects.requireNonNull(paramtypes);
        }

        @Override
        public Instruction getInstruction(Token[] tokens,
                int vregs, int ins, int outs, int inum,
                Map<String, Integer> indentifiers) throws ParseException {
            if (tokens.length != paramtypes.length) {
                throw new IllegalArgumentException();
            }
            Object[] out = new Object[paramtypes.length];
            for (int i = 0; i < out.length; i++) {
                ParamType type = paramtypes[i];
                switch (type) {
                    case Register:
                        out[i] = type.parse(tokens[i], vregs, ins, outs);
                        break;
                    case Identifier:
                        out[i] = type.parse(tokens[i], indentifiers, inum);
                        break;
                    default:
                        out[i] = type.parse(tokens[i]);
                        break;
                }
            }
            return ict.getInstruction(out);
        }

        public Instruction getDefalut() {
            Object[] out = new Object[paramtypes.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = paramtypes[i].getDefault();
            }
            return ict.getInstruction(out);
        }

        @Override
        public int getParamsCount() {
            return paramtypes.length;
        }
    }
}
