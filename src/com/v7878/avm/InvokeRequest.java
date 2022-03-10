package com.v7878.avm;

import java.nio.ByteBuffer;

public class InvokeRequest {

    final Node node;
    final ByteBuffer input;
    final int ret;
    final int rsize;

    public InvokeRequest(Node node, ByteBuffer input, int ret, int rsize) {
        this.node = node;
        this.input = input;
        this.ret = ret;
        this.rsize = rsize;
    }
}
