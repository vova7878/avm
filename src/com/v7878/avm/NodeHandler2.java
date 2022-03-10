package com.v7878.avm;

import com.v7878.avm.threads.StackElement;
import java.nio.ByteBuffer;

@FunctionalInterface
public interface NodeHandler2 extends NodeHandler {

    default InvokeRequest handle(StackElement current) {
        handle(current.node, current.vdata);
        return null;
    }

    void handle(Node node, ByteBuffer data);
}
