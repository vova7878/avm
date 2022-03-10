package com.v7878.avm.threads;

import com.v7878.avm.InvokeRequest;
import com.v7878.avm.Node;
import java.nio.ByteBuffer;

//TODO more safe
public class StackElement {

    public final Node node;
    public final ByteBuffer vdata;
    public int codepoint;
    public InvokeRequest req;

    public StackElement(Node node, ByteBuffer vdata, int codepoint) {
        this.node = node;
        this.vdata = vdata;
        this.codepoint = codepoint;
    }
}
