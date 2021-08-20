package com.v7878.avm;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface NodeHandler {

    void handle(Node node, ByteBuffer data);
}
