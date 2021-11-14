package com.v7878.avm;

import static com.v7878.avm.utils.NewApiUtils.slice;

import com.v7878.avm.Machine.MetadataCreator;
import com.v7878.avm.Metadata.InvokeInfo;
import com.v7878.avm.utils.NewApiUtils;

import java.nio.ByteBuffer;

public class Node {

    static void init() {
        Machine.init(Node::new,
                Node::invoke,
                (node, add) -> node.info.iinfo.invokationCount += add ? 1 : -1,
                (node, flags) -> node.info.flags = flags);
    }

    private final ByteBuffer data;
    private final Metadata info;

    @SuppressWarnings("LeakingThisInConstructor")
    private Node(ByteBuffer data, MetadataCreator info) {
        this.data = data;
        this.info = info.get(this);
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

    public ByteBuffer getData() {
        return fixOrder(data.duplicate());
    }

    public int getIndex() {
        return info.index;
    }

    private ByteBuffer invoke(ByteBuffer in) {
        InvokeInfo iinfo = info.iinfo;
        if (iinfo == null) {
            return null;
        }
        ByteBuffer bb = Machine.allocate(iinfo.regs);
        if (in != null) {
            int ins = Math.min(iinfo.ins, in.remaining());
            NewApiUtils.put(bb, iinfo.regs - iinfo.outs - iinfo.ins, in, in.position(), ins);
        }
        iinfo.h.handle(this, bb);
        bb.clear();
        return fixOrder(slice(bb, iinfo.regs - iinfo.outs, iinfo.outs));
    }
}
