package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class MulInt16 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("mul-int16", new NodeParser.SimpleInstructionCreator(
                (objs) -> new MulInt16((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public MulInt16(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        short a = data.getShort(A);
        short b = data.getShort(B);
        data.putShort(C, (short) (a * b));
    }
}
