package com.v7878.avm.utils;

public class Token {

    public final String data;
    public final int start;

    public Token(String data, int start) {
        this.data = data;
        this.start = start;
    }

    @Override
    public String toString() {
        return start + ":\"" + data + "\"";
    }
}
