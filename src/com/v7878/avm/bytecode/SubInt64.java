package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class SubInt64 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("sub-int64", new SimpleInstructionCreator(
                (objs) -> new SubInt64((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public SubInt64(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        long a = data.getLong(A);
        long b = data.getLong(B);
        data.putLong(C, a - b);
    }
}
