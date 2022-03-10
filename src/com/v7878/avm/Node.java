package com.v7878.avm;

import com.v7878.avm.Metadata.InvokeInfo;
import com.v7878.avm.interfaces.INode;
import com.v7878.avm.interfaces.INode.MetadataCreator;
import com.v7878.avm.threads.StackElement;
import java.nio.ByteBuffer;

public final class Node {

    static void init() {
        Machine.init(new INode() {
            @Override
            public InvokeRequest invoke(StackElement current) {
                return current.node.invoke(current);
            }

            @Override
            public void putFlags(Node node, int flags) {
                node.info.flags = flags;
            }

            @Override
            public int count(Node node, boolean add) {
                return node.info.iinfo.invocationCount += add ? 1 : -1;
            }

            @Override
            public Node createNode(ByteBuffer data, MetadataCreator creator) {
                return new Node(data, creator);
            }
        });
    }

    private final ByteBuffer data;
    private final Metadata info;

    @SuppressWarnings("LeakingThisInConstructor")
    private Node(ByteBuffer data, MetadataCreator creator) {
        this.data = data;
        this.info = creator.createMetadata(this);
    }

    public static ByteBuffer fixOrder(ByteBuffer bb) {
        bb.order(Constants.ENDIAN);
        return bb;
    }

    public int size() {
        return data.capacity();
    }

    public int getFlags() {
        return info.flags;
    }

    public boolean withFlags(int flags) {
        return (info.flags & flags) != 0;
    }

    //TODO: check safe
    public ByteBuffer getData() {
        return fixOrder(data.duplicate());
    }

    public int getIndex() {
        return info.index;
    }

    public int getInputOffset() {
        InvokeInfo iinfo = info.iinfo;
        if (iinfo == null) {
            return 0;
        }
        return iinfo.regs - iinfo.ins - iinfo.outs;
    }

    public int getOutputOffset() {
        InvokeInfo iinfo = info.iinfo;
        if (iinfo == null) {
            return 0;
        }
        return iinfo.regs - iinfo.outs;
    }

    public int getInputsCount() {
        InvokeInfo iinfo = info.iinfo;
        if (iinfo == null) {
            return 0;
        }
        return iinfo.ins;
    }

    public int getOutputsCount() {
        InvokeInfo iinfo = info.iinfo;
        if (iinfo == null) {
            return 0;
        }
        return iinfo.outs;
    }

    public int getRegistersCount() {
        InvokeInfo iinfo = info.iinfo;
        if (iinfo == null) {
            return 0;
        }
        return iinfo.regs;
    }

    private InvokeRequest invoke(StackElement current) {
        InvokeInfo iinfo = info.iinfo;
        if (iinfo == null) {
            return null;
        }
        return iinfo.h.handle(current);
    }
}
