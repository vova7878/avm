package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class SubInt32 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("sub-int32", new SimpleInstructionCreator(
                (objs) -> new SubInt32((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public SubInt32(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        int a = data.getInt(A);
        int b = data.getInt(B);
        data.putInt(C, a - b);
    }
}
