package com.v7878.avm.bytecode;

import com.v7878.avm.Node;
import com.v7878.avm.utils.DualBuffer;

public abstract class SimpleInstruction implements Instruction {

    @Override
    public int handle(Node node, DualBuffer data, Interpreter inter, boolean[] end) {
        handle(data);
        return 1;
    }

    public abstract void handle(DualBuffer data);
}
