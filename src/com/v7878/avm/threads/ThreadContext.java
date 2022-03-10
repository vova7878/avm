package com.v7878.avm.threads;

import com.v7878.avm.Machine;
import com.v7878.avm.Node;
import com.v7878.avm.utils.NewApiUtils;
import java.nio.ByteBuffer;

public final class ThreadContext {

    public static final int MAX_STACK_SIZE = 1024;
    public static final int MAX_REG_SIZE = 1024 * 1024; // 1 MiB
    public static final int DEFAULT_STACK_SIZE = 128;
    public static final int DEFAULT_REG_SIZE = 1024 * 256; // 0.25 MiB

    private static final ThreadLocal<ThreadContext> currentThreadContext
            = ThreadLocal.withInitial(() -> new ThreadContext(Thread.currentThread(),
            ThreadContext.DEFAULT_STACK_SIZE, ThreadContext.DEFAULT_REG_SIZE));

    private final Thread thread;
    private int currentStackElement = -1;
    private final StackElement[] stack;
    private int currentReg;
    private final ByteBuffer regs;

    private ThreadContext(Thread thread, int stackSize, int regSize) {
        if (stackSize <= 0 || stackSize > MAX_STACK_SIZE) {
            throw new IllegalArgumentException();
        }
        if (regSize < 0 || regSize > MAX_REG_SIZE) {
            throw new IllegalArgumentException();
        }
        this.thread = thread;
        this.stack = new StackElement[stackSize];
        this.regs = Machine.allocate(regSize);
    }

    public static ThreadContext getCurrent() {
        return currentThreadContext.get();
    }

    public Thread getThread() {
        return thread;
    }

    private ByteBuffer nextRegs(int regsCount) {
        ByteBuffer out = Node.fixOrder(NewApiUtils.slice(regs, currentReg, regsCount));
        currentReg += regsCount;
        return out;
    }

    public StackElement nextStackElement(Node node) {
        ByteBuffer vdata = nextRegs(node.getRegistersCount());
        currentStackElement += 1;
        StackElement se = new StackElement(node, vdata, 0);
        stack[currentStackElement] = se;
        return se;
    }

    public void deleteCurrent() {
        StackElement current = stack[currentStackElement];
        stack[currentStackElement] = null;
        int size = current.vdata.capacity();
        currentReg -= size;
        NewApiUtils.fillZeros(regs, currentReg, size);
        currentStackElement--;
    }

    public StackElement getPrevious() {
        return currentStackElement == 0 ? null : stack[currentStackElement - 1];
    }
}
