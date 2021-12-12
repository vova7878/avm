package com.v7878.avm.utils;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private final String data;
    private final List<Token> tokens = new ArrayList<>();
    private StringBuilder tmp = new StringBuilder();
    private int i;
    private boolean str;
    private boolean nw;

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
            i++;
            tmp.append(c);
            if (i == data.length()) {
                return true;
            }
            char c2 = data.charAt(i);
            i++;
            if (Character.isWhitespace(c2)) {
                return false;
            }
            tmp.append(c2);
            return true;
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
            tmp.append(c);
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
}
