package com.v7878.avm;

import static com.v7878.avm.Constants.NODE_DELETED;
import static com.v7878.avm.Constants.NODE_INDELIBLE;
import static com.v7878.avm.Constants.NODE_NAMED;
import com.v7878.avm.Metadata.InvokeInfo;
import com.v7878.avm.bytecode.Instruction;
import com.v7878.avm.bytecode.Interpreter;
import com.v7878.avm.utils.NewApiUtils;
import com.v7878.avm.utils.Tree16;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Machine {

    private static NodeCreator creator;
    private static NodeInvoker invoker;
    private static NodeFlags flags;
    private static Machine vm;

    static {
        Node.init();
    }

    static void init(NodeCreator creator, NodeInvoker invoker, NodeFlags flags) {
        Machine.creator = creator;
        Machine.invoker = invoker;
        Machine.flags = flags;
    }

    public static Machine get() {
        if (vm == null) {
            vm = new Machine();
        }
        return vm;
    }

    private final Tree16<Node> nodes = new Tree16<>();
    private final Map<String, Node> names = new HashMap<>();

    private Machine() {
    }

    public Node newNode(int regs) {
        if (regs < 0) {
            throw new IllegalArgumentException();
        }
        return creator.create(allocate(regs), (node) -> new Metadata(putNode(node), null));
    }

    public Node newNode(ByteBuffer data) {
        return newNode(data, true);
    }

    Node newNode(ByteBuffer in, boolean clone) {
        if (in == null) {
            throw new NullPointerException();
        }
        ByteBuffer data;
        if (clone) {
            data = allocate(in.remaining());
            data.put(in).position(0);
        } else {
            data = in;
        }
        return creator.create(data, (node) -> new Metadata(putNode(node), null));
    }

    public Node newNode(NodeHandler h, int ins, int outs) {
        if (outs < 0 || ins < 0 || (ins + outs < 0)) {
            throw new IllegalArgumentException();
        }
        return creator.create(allocate(0), (node) -> new Metadata(putNode(node), new InvokeInfo(ins + outs, ins, outs, h)));
    }

    public Node newNode(Instruction[] instrs, int vregs, int ins, int outs) {
        if (outs < 0 || ins < 0 || (ins + outs < 0)) {
            throw new IllegalArgumentException();
        }
        if (vregs < ins + outs) {
            throw new IllegalArgumentException();
        }
        return creator.create(allocate(0), (node) -> new Metadata(putNode(node),
                new InvokeInfo(vregs, ins, outs, new Interpreter(instrs))));
    }

    public Node newNode(ByteBuffer in, Instruction[] instrs, int vregs, int ins, int outs) {
        return newNode(in, true, instrs, vregs, ins, outs);
    }

    Node newNode(ByteBuffer in, boolean clone, Instruction[] instrs, int vregs, int ins, int outs) {
        if (in == null) {
            throw new NullPointerException();
        }
        if (outs < 0 || ins < 0 || (ins + outs < 0)) {
            throw new IllegalArgumentException();
        }
        if (vregs < ins + outs) {
            throw new IllegalArgumentException();
        }
        ByteBuffer data;
        if (clone) {
            data = allocate(in.remaining());
            data.put(in).position(0);
        } else {
            data = in;
        }
        return creator.create(data, (node) -> new Metadata(putNode(node),
                new InvokeInfo(vregs, ins, outs, new Interpreter(instrs))));
    }

    static ByteBuffer allocate(int size) {
        return Node.setOrder(ByteBuffer.allocate(size));
    }

    public ByteBuffer invoke(Node node, ByteBuffer in) {
        synchronized (node) {
            if (node.withFlags(NODE_DELETED)) {
                throw new IllegalStateException("node deleted");
            }
            return invoker.invoke(node, in);
        }
    }

    public Node getNode(int index) {
        return nodes.get(index - 1);
    }

    public void setNodeName(Node node, String name) {
        Objects.requireNonNull(name);
        synchronized (node) {
            if (node.withFlags(NODE_DELETED | NODE_NAMED)) {
                throw new IllegalStateException("can not set node name");
            }
            if (NewApiUtils.putIfAbsent(names, name, node) != null) {
                throw new IllegalStateException("name is already in use");
            }
            flags.put(node, node.getFlags() | NODE_NAMED);
        }
    }

    public Node findNode(String name) {
        Node out = names.get(name);
        if (out == null) {
            throw new RuntimeException("node \"" + name + "\" not found");
        }
        return out;
    }

    private Node deleteNode(int index) {
        synchronized (nodes) {
            return nodes.remove(index - 1);
        }
    }

    public Node deleteNode(Node node) {
        synchronized (node) {
            if (node.withFlags(NODE_DELETED | NODE_INDELIBLE | NODE_NAMED)) {
                throw new IllegalStateException("can not delete node");
            }
            Node out = deleteNode(node.getIndex());
            flags.put(node, node.getFlags() | NODE_DELETED);
            if (node != out) {
                throw new IllegalStateException("different nodes");
            }
            return out;
        }
    }

    private int putNode(Node value) {
        synchronized (nodes) {
            return nodes.put(value) + 1;
        }
    }

    @FunctionalInterface
    interface MetadataCreator {

        Metadata get(Node n);
    }

    @FunctionalInterface
    interface NodeInvoker {

        ByteBuffer invoke(Node node, ByteBuffer in);
    }

    @FunctionalInterface
    interface NodeFlags {

        void put(Node node, int flags);
    }

    @FunctionalInterface
    interface NodeCreator {

        Node create(ByteBuffer data, MetadataCreator info);
    }
}
