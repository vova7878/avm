package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class ShlInt32 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("shl-int32", new SimpleInstructionCreator(
                (objs) -> new ShlInt32((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public ShlInt32(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        int a = data.getInt(A);
        int b = data.get(B);
        data.putInt(C, a << b);
    }
}
