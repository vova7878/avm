package com.v7878.avm.exceptions;

import com.v7878.avm.utils.Token;

public class ParseTokenException extends ParseException {

    private final Token token;

    public ParseTokenException(Token t) {
        super("can`t parse token " + t);
        token = t;
    }

    public ParseTokenException(Token t, String msg) {
        super(msg + t);
        token = t;
    }

    public Token getToken() {
        return token;
    }
}
