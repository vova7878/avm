package com.v7878.avm.bytecode;

import com.v7878.avm.InvokeRequest;
import com.v7878.avm.NodeHandler;
import com.v7878.avm.threads.StackElement;
import com.v7878.avm.utils.DualBuffer;

public class Interpreter implements NodeHandler {

    private final Instruction[] instrs;

    public Interpreter(Instruction[] instrs) {
        this.instrs = instrs;
    }

    @Override
    public InvokeRequest handle(StackElement current) {
        DualBuffer data = new DualBuffer(current.node.getData(), current.vdata);
        InvokeRequest[] req = new InvokeRequest[1];
        boolean[] end = new boolean[1];
        while (!(end[0] || req[0] != null)) {
            current.codepoint += instrs[current.codepoint].handle(current.node, data, this, req, end);
        }
        return req[0];
    }
}
