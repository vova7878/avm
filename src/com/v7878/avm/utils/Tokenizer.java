package com.v7878.avm.utils;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private final String data;
    private final List<Token> tokens = new ArrayList<>();
    private StringBuilder tmp = new StringBuilder();
    private int i;
    private boolean str;
    private boolean nw = false;

    public Tokenizer(String data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data;
    }

    public Token[] parseTokens() {
        int start = -1;
        while (i < data.length()) {
            int li = i;
            boolean t = next();
            if (!nw && t) {
                start = li;
                nw = true;
            } else if (nw && !t) {
                if (start == -1) {
                    throw new IllegalStateException();
                }
                tokens.add(new Token(tmp.toString(), start));
                tmp = new StringBuilder();
                start = -1;
                nw = false;
            }
        }
        if (nw) {
            if (start == -1) {
                throw new IllegalStateException();
            }
            tokens.add(new Token(tmp.toString(), start));
            tmp = new StringBuilder();
            nw = false;
        }
        if (str) {
            throw new IllegalArgumentException("unclosed string");
        }
        return tokens.toArray(new Token[0]);
    }

    private boolean next() {
        char c = data.charAt(i);
        if (c == '"') {
            i++;
            str ^= true;
            tmp.append(c);
            return true;
        }
        if (c == '\\') {
            if (str) {
                tmp.append(c);
                char c2 = data.charAt(i + 1);
                tmp.append(c2);
                if (c2 == 'u') {
                    String ss = data.substring(i + 2, i + 6);
                    if (!ss.matches("[0-9a-fA-F]{4}")) {
                        throw new IllegalArgumentException("unknown char '\\u" + ss + "' at position: " + i);
                    }
                    i += 6;
                    tmp.append(ss);
                } else {
                    switch (c2) {
                        case '\\':
                        case 't':
                        case 'b':
                        case 'n':
                        case 'f':
                        case 'r':
                        case '"':
                        case '\'':
                            i += 2;
                            break;
                        default:
                            throw new IllegalArgumentException("'\\" + c2 + "' at position: " + i);
                    }
                }
                return true;
            } else {
                throw new IllegalArgumentException("'\\' outside string");
            }
        }
        if (!nw && c == '#') {
            i++;
            for (; i < data.length(); i++) {
                if (data.charAt(i) == '\n') {
                    i++;
                    break;
                }
            }
            return false;
        }
        if (str) {
            if (c == ' ') {
                tmp.append(' ');
            } else if (Character.isWhitespace(c)) {
                throw new IllegalArgumentException("illegal char '" + c + "' at position: " + i);
            } else {
                tmp.append(c);
            }
            i++;
            return true;
        }
        i++;
        if (Character.isWhitespace(c)) {
            return false;
        }
        tmp.append(c);
        return true;
    }

    public static class Token {

        public final String data;
        public final int start;

        public Token(String data, int start) {
            this.data = data;
            this.start = start;
        }

        @Override
        public String toString() {
            return start + ": [" + data + "]";
        }
    }
}
