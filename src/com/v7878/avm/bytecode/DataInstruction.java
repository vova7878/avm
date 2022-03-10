package com.v7878.avm.bytecode;

import com.v7878.avm.InvokeRequest;
import com.v7878.avm.Node;
import com.v7878.avm.utils.DualBuffer;

public abstract class DataInstruction implements Instruction {

    @Override
    public int handle(Node node, DualBuffer data, Interpreter inter, InvokeRequest[] req, boolean[] end) {
        handle(node, data);
        return 1;
    }

    public abstract void handle(Node thiz, DualBuffer data);
}
