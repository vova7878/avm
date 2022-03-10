package com.v7878.avm.interfaces;

import com.v7878.avm.InvokeRequest;
import com.v7878.avm.Metadata;
import com.v7878.avm.Node;
import com.v7878.avm.threads.StackElement;
import java.nio.ByteBuffer;

public interface INode {

    InvokeRequest invoke(StackElement current);

    void putFlags(Node node, int flags);

    int count(Node node, boolean add);

    Node createNode(ByteBuffer data, MetadataCreator creator);

    @FunctionalInterface
    public static interface MetadataCreator {

        Metadata createMetadata(Node n);
    }
}
