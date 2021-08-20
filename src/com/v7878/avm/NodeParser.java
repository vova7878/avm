package com.v7878.avm;

import com.v7878.avm.bytecode.Init;
import com.v7878.avm.bytecode.Instruction;
import com.v7878.avm.utils.DualBuffer;
import com.v7878.avm.utils.NewApiUtils;
import com.v7878.avm.utils.Pair;
import com.v7878.avm.utils.Tokenizer;
import com.v7878.avm.utils.Tokenizer.Token;
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
    public static final String ALL_INT = ALL_INT_NO_POSTFIX + INT_POSTFIXES + "?";
    public static final String STRING = "\"( |\\\\u[0-9a-fA-F]{4}|\\\\t|\\\\b|\\\\n|\\\\f|\\\\r|\\\\\"|\\\\'|\\\\\\\\|[^\"\\s\\\\])*\"";
    public static final String REGISTER = "[dDvVpPrR]" + SIMPLE_UINT;
    public static final String IDENTIFIER = ":\\w+";
    public static final String INSTRUCTION_NAME = "\\w(-?\\w+)*";
    public static final String NAMED_INSTRUCTION_PARAMETER = "(?<value>" + STRING + "|" + ALL_INT + "|" + REGISTER + "|" + IDENTIFIER + ")";

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

    public static Node[] parseNodes(String data) {
        Token[] tokens = new Tokenizer(data).parseTokens();
        List<Token[]> nodes = new ArrayList<>();
        int start = -1;
        for (int i = 0; i < tokens.length; i++) {
            switch (tokens[i].data) {
                case ".node":
                    if (start == -1) {
                        start = i;
                    } else {
                        throw new IllegalStateException();
                    }
                    break;
                case ".endnode":
                    if (start != -1) {
                        Token[] node = new Token[i - start - 1];
                        System.arraycopy(tokens, start + 1, node, 0, node.length);
                        nodes.add(node);
                        start = -1;
                    } else {
                        throw new IllegalStateException();
                    }
                    break;
                default:
                    if (i == 0) {
                        throw new IllegalStateException();
                    }
                    break;
            }
        }
        if (start != -1) {
            throw new IllegalStateException("unclosed node block");
        }
        Node[] out = new Node[nodes.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = parseNode(nodes.get(i));
        }
        return out;
    }

    private static Node parseNode(Token[] tokens) {
        String name = tokens[0].data;
        if (!name.matches(STRING)) {
            throw new IllegalStateException("unknown token at position: " + tokens[0].start);
        }
        name = (String) ParamType.String.parse(name);
        int i = 1;
        if (i == tokens.length) {
            return Machine.get().newNode(0);
        }
        ByteBuffer data = null;
        Node out = null;
        ret:
        {
            if (tokens[i].data.equals(".data")) {
                boolean end = false;
                for (; i < tokens.length; i++) {
                    if (tokens[i].data.equals(".enddata")) {
                        end = true;
                        break;
                    }
                }
                if (!end) {
                    throw new IllegalStateException("unclosed data block");
                }
                data = parseData(Arrays.copyOfRange(tokens, 2, i));
                i++;
                if (i == tokens.length) {
                    out = Machine.get().newNode(data, false);
                    break ret;
                }
            }
            if (!tokens[i].data.equals(".code")) {
                throw new IllegalStateException("unknown token at position: " + tokens[i].start);
            }
            if (!tokens[tokens.length - 1].data.equals(".endcode")) {
                throw new IllegalStateException("unknown token at position: " + tokens[tokens.length - 1].start);
            }
            Object[] code = parseCode(Arrays.copyOfRange(tokens, i + 1, tokens.length - 1));
            Instruction[] instrs = (Instruction[]) code[0];
            int vregs = (int) code[1], ins = (int) code[2], outs = (int) code[3];
            if (data == null) {
                out = Machine.get().newNode(instrs, vregs, ins, outs);
                break ret;
            }
            out = Machine.get().newNode(data, false, instrs, vregs, ins, outs);
            break ret;
        }
        Machine.get().setNodeName(out, name);
        return out;
    }

    private static ByteBuffer parseData(Token[] tokens) {
        int size = (int) ParamType.SimpleUInt.parse(tokens[0].data);
        ByteBuffer out = Machine.allocate(size);
        for (int i = 1; i < tokens.length; i++) {
            String data = tokens[i].data;
            if (data.matches(ALL_INT_NO_POSTFIX)) {
                out.putInt((int) ParamType.Int32.parse(data));
            } else if (data.matches(ALL_INT_WITH_POSTFIX)) {
                switch (data.charAt(data.length() - 1)) {
                    case 'Z':
                    case 'z':
                        out.put((byte) ParamType.Int8.parse(data));
                        break;
                    case 'S':
                    case 's':
                        out.putShort((short) ParamType.Int16.parse(data));
                        break;
                    case 'L':
                    case 'l':
                        out.putLong((long) ParamType.Int64.parse(data));
                        break;
                    case 'W':
                    case 'w':
                        DualBuffer.putWide(out, (Wide) ParamType.Int128.parse(data));
                    default:
                        throw new IllegalStateException();
                }
            } else {
                throw new IllegalStateException("unknown token at position: " + tokens[i].start);
            }
        }
        return out;
    }

    private static Object[] parseCode(Token[] tokens) {
        Object[] out = new Object[4];
        int vregs = (int) (out[1] = ParamType.SimpleUInt.parse(tokens[0].data));
        int ins = (int) (out[2] = ParamType.SimpleUInt.parse(tokens[1].data));
        int outs = (int) (out[3] = ParamType.SimpleUInt.parse(tokens[2].data));
        List<Pair<InstructionCreator, String[]>> instrs = new ArrayList<>();
        Map<String, Integer> indentifiers = new HashMap<>();
        for (int i = 3, t = 0; i < tokens.length; t++) {
            if (tokens[i].data.matches(IDENTIFIER)) {
                indentifiers.put(tokens[i].data, t);
                i++;
            }
            String iname = tokens[i].data;
            if (!iname.matches(INSTRUCTION_NAME)) {
                throw new IllegalStateException("unknown token at position: " + tokens[i].start);
            }
            i++;
            InstructionCreator ict = icreator.get(iname);
            if (ict == null) {
                throw new IllegalStateException("unknown instruction: " + iname);
            }
            String[] iparams = new String[ict.getParamsCount()];
            for (int g = 0; g < iparams.length; g++, i++) {
                iparams[g] = tokens[i].data;
            }
            instrs.add(new Pair<>(ict, iparams));
        }
        Instruction[] ii = new Instruction[instrs.size()];
        for (int i = 0; i < ii.length; i++) {
            Pair<InstructionCreator, String[]> data = instrs.get(i);
            ii[i] = data.a.getInstruction(data.b, vregs, ins, outs, i, indentifiers);
        }
        out[0] = ii;
        return out;
    }

    public enum ParamType {
        Register() {
            @Override
            public Integer parse(String iparam, Object... params2) {
                if (!iparam.matches(REGISTER)) {
                    throw new IllegalArgumentException();
                }
                int vregs = (int) params2[0], ins = (int) params2[1], outs = (int) params2[2];
                iparam = iparam.toLowerCase();
                char type = iparam.charAt(0);
                int reg = NewApiUtils.parseInt(iparam, 1, iparam.length(), 10);
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
                        throw new IllegalStateException("unknown register type");
                }
            }

            @Override
            public Integer getDefault() {
                return 0;
            }
        }, Identifier() {
            @Override
            public Integer parse(String iparam, Object... params2) {
                if (!iparam.matches(IDENTIFIER)) {
                    throw new IllegalArgumentException();
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
            public Integer parse(String iparam, Object... params2) {
                if (!iparam.matches(SIMPLE_UINT)) {
                    throw new IllegalArgumentException();
                }
                return Integer.parseInt(iparam);
            }

            @Override
            public Integer getDefault() {
                return 0;
            }
        }, Int8() {
            @Override
            public Byte parse(String iparam, Object... params2) {
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
                    throw new IllegalArgumentException();
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
            public Short parse(String iparam, Object... params2) {
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
                    throw new IllegalArgumentException();
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
            public Integer parse(String iparam, Object... params2) {
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
                    throw new IllegalArgumentException();
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
            public Long parse(String iparam, Object... params2) {
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
                    throw new IllegalArgumentException();
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
            public Wide parse(String iparam, Object... params2) {
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
                    throw new IllegalArgumentException();
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
            public String parse(String iparam, Object... params2) {
                if (!iparam.matches(STRING)) {
                    throw new IllegalArgumentException();
                }
                iparam = iparam.substring(1, iparam.length() - 1);
                StringBuilder out = new StringBuilder();
                for (int i = 0; i < iparam.length();) {
                    char c = iparam.charAt(i);
                    if (c == '\\') {
                        char c2 = iparam.charAt(i + 1);
                        if (c2 == 'u') {
                            String ss = iparam.substring(i + 2, i + 6);
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
                                    throw new IllegalStateException();
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

        public abstract Object parse(String iparam, Object... params2);

        public abstract Object getDefault();
    }

    public interface InstructionCreator {

        Instruction getInstruction(String[] iparams, int vregs, int ins, int outs, int inum, Map<String, Integer> indentifiers);

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
        public Instruction getInstruction(String[] iparams, int vregs, int ins, int outs, int inum, Map<String, Integer> indentifiers) {
            if (iparams.length != paramtypes.length) {
                throw new IllegalArgumentException();
            }
            Object[] out = new Object[paramtypes.length];
            for (int i = 0; i < out.length; i++) {
                ParamType type = paramtypes[i];
                switch (type) {
                    case Register:
                        out[i] = type.parse(iparams[i], vregs, ins, outs);
                        break;
                    case Identifier:
                        out[i] = type.parse(iparams[i], indentifiers, inum);
                        break;
                    default:
                        out[i] = type.parse(iparams[i]);
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
