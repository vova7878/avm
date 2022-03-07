package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class OrInt16 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("or-int16", new SimpleInstructionCreator(
                (objs) -> new OrInt16((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public OrInt16(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        short a = data.getShort(A);
        short b = data.getShort(B);
        data.putShort(C, (short) (a | b));
    }
}
