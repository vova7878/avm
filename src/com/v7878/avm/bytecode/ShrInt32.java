package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class ShrInt32 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("shr-int32", new NodeParser.SimpleInstructionCreator(
                (objs) -> new ShrInt32((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public ShrInt32(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        int a = data.getInt(A);
        int b = data.get(B);
        data.putInt(C, a >> b);
    }
}
