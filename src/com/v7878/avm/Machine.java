package com.v7878.avm;

import static com.v7878.avm.Constants.NODE_DELETED;
import static com.v7878.avm.Constants.NODE_INDELIBLE;
import static com.v7878.avm.Constants.NODE_INVOKED;
import static com.v7878.avm.Constants.NODE_NAMED;
import static com.v7878.avm.Constants.NODE_PRIVATE;
import static com.v7878.avm.Constants.NODE_PROTECTED;
import com.v7878.avm.Metadata.InvokeInfo;
import com.v7878.avm.bytecode.Instruction;
import com.v7878.avm.bytecode.Interpreter;
import com.v7878.avm.utils.NewApiUtils;
import com.v7878.avm.utils.Tree;
import com.v7878.avm.utils.Tree16;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Machine {

    private static NodeCreator creator;
    private static NodeInvoker invoker;
    private static NodeInvocationCounter icounter;
    private static NodeFlags flags;
    private static Machine vm;
    private static final int ALLOWED_FLAGS = NODE_PRIVATE | NODE_PROTECTED | NODE_INDELIBLE;

    static {
        Node.init();
    }

    static void init(NodeCreator creator, NodeInvoker invoker, NodeInvocationCounter icounter, NodeFlags flags) {
        Machine.creator = creator;
        Machine.invoker = invoker;
        Machine.icounter = icounter;
        Machine.flags = flags;
    }

    public static Machine get() {
        if (vm == null) {
            vm = new Machine();
        }
        return vm;
    }

    private final Tree<Node> nodes = new Tree16<>();
    private final Map<String, Node> names = new HashMap<>();

    private Machine() {
    }

    public Node newNode(int flags, int regs) {
        checkFlags(flags);
        if (regs < 0) {
            throw new IllegalArgumentException();
        }
        return creator.create(allocate(regs), (node)
                -> new Metadata(putNode(node), null, flags));
    }

    public Node newNode(int regs) {
        return newNode(0, regs);
    }

    public Node newNode(ByteBuffer data) {
        return newNode(0, data);
    }

    public Node newNode(int flags, ByteBuffer data) {
        return newNode(flags, data, true);
    }

    Node newNode(int flags, ByteBuffer in, boolean clone) {
        checkFlags(flags);
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
        return creator.create(data, (node)
                -> new Metadata(putNode(node), null, flags));
    }

    public Node newNode(NodeHandler h, int ins, int outs) {
        return newNode(0, h, ins, outs);
    }

    public Node newNode(int flags, NodeHandler h,
            int ins, int outs) {
        checkFlags(flags);
        if (outs < 0 || ins < 0 || (ins + outs < 0)) {
            throw new IllegalArgumentException();
        }
        return creator.create(allocate(0), (node)
                -> new Metadata(putNode(node),
                        new InvokeInfo(ins + outs, ins, outs, h),
                        flags));
    }

    public Node newNode(Instruction[] instrs,
            int vregs, int ins, int outs) {
        return newNode(0, instrs, vregs, ins, outs);
    }

    public Node newNode(int flags, Instruction[] instrs,
            int vregs, int ins, int outs) {
        checkFlags(flags);
        if (outs < 0 || ins < 0 || (ins + outs < 0)) {
            throw new IllegalArgumentException();
        }
        if (vregs < ins + outs) {
            throw new IllegalArgumentException();
        }
        return creator.create(allocate(0), (node) -> new Metadata(putNode(node),
                new InvokeInfo(vregs, ins, outs, new Interpreter(instrs)),
                flags));
    }

    public Node newNode(ByteBuffer in, Instruction[] instrs,
            int vregs, int ins, int outs) {
        return newNode(0, in, instrs, vregs, ins, outs);
    }

    public Node newNode(int flags, ByteBuffer in, Instruction[] instrs,
            int vregs, int ins, int outs) {
        return newNode(flags, in, true, instrs, vregs, ins, outs);
    }

    Node newNode(int flags, ByteBuffer in, boolean clone, Instruction[] instrs,
            int vregs, int ins, int outs) {
        checkFlags(flags);
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
                new InvokeInfo(vregs, ins, outs, new Interpreter(instrs)),
                flags));
    }

    static ByteBuffer allocate(int size) {
        return Node.fixOrder(ByteBuffer.allocate(size));
    }

    public ByteBuffer invoke(Node node, ByteBuffer in) {
        synchronized (node) {
            if (node.withFlags(NODE_DELETED)) {
                throw new IllegalStateException("Node deleted");
            }
            flags.put(node, node.getFlags() | NODE_INVOKED);
            icounter.count(node, true);
        }
        ByteBuffer out = invoker.invoke(node, in);
        synchronized (node) {
            if (icounter.count(node, false) == 0) {
                flags.put(node, node.getFlags() & ~NODE_INVOKED);
            }
        }
        return out;
    }

    public Node getNode(int index) {
        return nodes.get(index - 1);
    }

    public void setNodeName(Node node, String name) {
        Objects.requireNonNull(name);
        synchronized (node) {
            if (node.withFlags(NODE_DELETED | NODE_NAMED)) {
                throw new IllegalStateException("Can not set node name");
            }
            if (NewApiUtils.putIfAbsent(names, name, node) != null) {
                throw new IllegalStateException("Name \"" + name + "\" is already in use");
            }
            flags.put(node, node.getFlags() | NODE_NAMED);
        }
    }

    public Node findNode(String name) {
        Node out = names.get(name);
        if (out == null) {
            throw new RuntimeException("Node \"" + name + "\" not found");
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
            if (node.withFlags(NODE_DELETED | NODE_INDELIBLE | NODE_INVOKED | NODE_NAMED)) {
                throw new IllegalStateException("Can not delete node");
            }
            Node out = deleteNode(node.getIndex());
            flags.put(node, node.getFlags() | NODE_DELETED);
            if (node != out) {
                throw new IllegalStateException("Different nodes");
            }
            return out;
        }
    }

    private int putNode(Node value) {
        synchronized (nodes) {
            return nodes.put(value) + 1;
        }
    }

    private void checkFlags(int flags) {
        if ((flags & ~ALLOWED_FLAGS) != 0) {
            throw new IllegalArgumentException("Invalid flags");
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
    interface NodeInvocationCounter {

        int count(Node node, boolean add);
    }

    @FunctionalInterface
    interface NodeCreator {

        Node create(ByteBuffer data, MetadataCreator info);
    }
}
