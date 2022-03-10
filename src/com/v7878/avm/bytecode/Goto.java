package com.v7878.avm.bytecode;

import com.v7878.avm.InvokeRequest;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Identifier;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class Goto implements Instruction {

    static void init() {
        NodeParser.addCreator("goto", new SimpleInstructionCreator(
                (objs) -> new Goto((int) objs[0]),
                Identifier));
    }

    private final int A;

    public Goto(int A) {
        this.A = A;
    }

    @Override
    public int handle(Node node, DualBuffer data, Interpreter inter, InvokeRequest[] req, boolean[] end) {
        return A;
    }
}
