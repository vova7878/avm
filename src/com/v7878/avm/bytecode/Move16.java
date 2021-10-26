package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class Move16 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("move16", new NodeParser.SimpleInstructionCreator(
                (objs) -> new Move16((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public Move16(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putShort(B, data.getShort(A));
    }
}
