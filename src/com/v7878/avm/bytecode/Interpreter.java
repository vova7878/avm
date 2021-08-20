package com.v7878.avm.bytecode;

import com.v7878.avm.Node;
import com.v7878.avm.NodeHandler;
import com.v7878.avm.utils.DualBuffer;

import java.nio.ByteBuffer;

public class Interpreter implements NodeHandler {

    private final Instruction[] instrs;

    public Interpreter(Instruction[] instrs) {
        this.instrs = instrs;
    }

    @Override
    public void handle(Node node, ByteBuffer vdata) {
        DualBuffer data = new DualBuffer(node.getData(), vdata);
        int codepoint = 0;
        boolean[] end = new boolean[1];
        while (!end[0]) {
            codepoint += instrs[codepoint].handle(node, data, this, end);
        }
    }
}
