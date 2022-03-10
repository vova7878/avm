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
import com.v7878.avm.interfaces.INode;
import com.v7878.avm.threads.StackElement;
import com.v7878.avm.threads.ThreadContext;
import com.v7878.avm.utils.NewApiUtils;
import static com.v7878.avm.utils.NewApiUtils.put;
import static com.v7878.avm.utils.NewApiUtils.slice;
import com.v7878.avm.utils.Tree;
import com.v7878.avm.utils.Tree16;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Machine {

    private static INode inode;
    private static Machine vm;
    private static final int ALLOWED_FLAGS = NODE_PRIVATE | NODE_PROTECTED | NODE_INDELIBLE;

    static {
        Node.init();
    }

    static void init(INode inode) {
        if (Machine.inode != null) {
            throw new SecurityException();
        }
        Machine.inode = inode;
    }

    public static Machine get() {
        if (vm == null) {
            vm = new Machine();
        }
        return vm;
    }

    //TODO: thread-safe
    private final Tree<Node> nodes = new Tree16<>();
    private final Map<String, Node> names = new ConcurrentHashMap<>();

    private Machine() {
    }

    public Node newNode(int flags, int regs) {
        checkFlags(flags);
        if (regs < 0) {
            throw new IllegalArgumentException();
        }
        return inode.createNode(allocate(regs), (node)
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
        return inode.createNode(data, (node)
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
        return inode.createNode(allocate(0), (node)
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
        return inode.createNode(allocate(0), (node) -> new Metadata(putNode(node),
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
        return inode.createNode(data, (node) -> new Metadata(putNode(node),
                new InvokeInfo(vregs, ins, outs, new Interpreter(instrs)),
                flags));
    }

    public static ByteBuffer allocate(int size) {
        return Node.fixOrder(ByteBuffer.allocate(size));
    }

    private InvokeRequest startInvoke(StackElement current) {
        Node node = current.node;
        synchronized (node) {
            if (node.withFlags(NODE_DELETED)) {
                throw new IllegalStateException("Node deleted");
            }
            inode.putFlags(node, node.getFlags() | NODE_INVOKED);
            inode.count(node, true);
        }
        return inode.invoke(current);
    }

    private void endInvoke(Node node) {
        synchronized (node) {
            if (inode.count(node, false) == 0) {
                inode.putFlags(node, node.getFlags() & ~NODE_INVOKED);
            }
        }
    }

    private void fillInput(ByteBuffer in, StackElement se) {
        if (in != null) {
            ByteBuffer vdata = se.vdata;
            int ins = se.node.getInputsCount();
            int offset = se.node.getInputOffset();
            int write = Math.min(ins, in.remaining());
            put(vdata, offset, in, in.position(), write);
        }
    }

    private void fillOutput(ByteBuffer vdata, StackElement se,
            InvokeRequest req) {
        if (req.rsize == 0) {
            return;
        }
        put(vdata, req.ret, se.vdata,
                se.node.getOutputOffset(), req.rsize);
    }

    private void getOutput(Node node, ByteBuffer vdata, ByteBuffer out,
            int offset, int length) {
        int nodeOffset = node.getOutputOffset();
        put(out, offset, vdata, nodeOffset, length);
    }

    @SuppressWarnings("null")
    public void invoke(Node node, ByteBuffer in, int offsetIn, int lengthIn,
            ByteBuffer out, int offsetOut, int lengthOut) {
        ThreadContext current = ThreadContext.getCurrent();
        StackElement se = null;
        InvokeRequest req = new InvokeRequest(node,
                in == null ? null : slice(in, offsetIn, lengthIn), 0, 0);
        while (true) {
            if (req == null) {
                endInvoke(se.node);
                if (se.node == node) {
                    getOutput(node, se.vdata, out, offsetOut, lengthOut);
                    current.deleteCurrent();
                    break;
                }
                StackElement tmp = current.getPrevious();
                fillOutput(tmp.vdata, se, tmp.req);
                current.deleteCurrent();
                tmp.req = null;
                se = tmp;
            } else {
                if (se != null) {
                    se.req = req;
                }
                se = current.nextStackElement(req.node);
                fillInput(req.input, se);
            }
            req = startInvoke(se);
        }
    }

    public ByteBuffer invoke(Node node, ByteBuffer in) {
        ByteBuffer out = allocate(node.getOutputsCount());
        invoke(node, in, in == null ? 0 : in.position(), in == null ? 0 : in.remaining(),
                 out, 0, out.capacity());
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
            inode.putFlags(node, node.getFlags() | NODE_NAMED);
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
            inode.putFlags(node, node.getFlags() | NODE_DELETED);
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
}
