package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class ShrInt16 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("shr-int16", new NodeParser.SimpleInstructionCreator((objs) -> new ShrInt16((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public ShrInt16(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        int a = data.getShort(A);
        int b = data.get(B);
        data.putShort(C, (short) (a >> b));
    }
}
