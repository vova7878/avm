package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class Move32 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("move32", new NodeParser.SimpleInstructionCreator((objs) -> new Move32((int) objs[0], (int) objs[1]), Register, Register));
    }

    private final int A, B;

    public Move32(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putInt(B, data.getInt(A));
    }
}
