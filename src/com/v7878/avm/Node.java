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
                (node, flags) -> node.info.flags = flags);
    }

    private final ByteBuffer data;
    private final Metadata info;

    @SuppressWarnings("LeakingThisInConstructor")
    private Node(ByteBuffer data, MetadataCreator info) {
        this.data = data;
        this.info = info.get(this);
    }

    public static ByteBuffer setOrder(ByteBuffer bb) {
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
        return setOrder(data.duplicate());
    }

    public int getIndex() {
        return info.index;
    }

    private ByteBuffer invoke(ByteBuffer in) {
        InvokeInfo invoke = info.invoke;
        if (invoke == null) {
            return null;
        }
        ByteBuffer bb = Machine.allocate(invoke.regs);
        if (in != null) {
            int ins = Math.min(invoke.ins, in.remaining());
            NewApiUtils.put(bb, invoke.regs - invoke.outs - invoke.ins, in, in.position(), ins);
        }
        invoke.h.handle(this, bb);
        bb.clear();
        return setOrder(slice(bb, invoke.regs - invoke.outs, invoke.outs));
    }
}
