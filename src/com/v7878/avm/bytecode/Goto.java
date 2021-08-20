package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Identifier;

import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class Goto implements Instruction {

    static void init() {
        NodeParser.addCreator("goto", new NodeParser.SimpleInstructionCreator((objs) -> new Goto((int) objs[0]), Identifier));
    }

    private final int A;

    public Goto(int A) {
        this.A = A;
    }

    @Override
    public int handle(Node node, DualBuffer data, Interpreter inter, boolean[] end) {
        return A;
    }
}
