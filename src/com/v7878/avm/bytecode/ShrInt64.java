package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class ShrInt64 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("shr-int64", new SimpleInstructionCreator(
                (objs) -> new ShrInt64((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public ShrInt64(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        long a = data.getLong(A);
        int b = data.get(B);
        data.putLong(C, a >> b);
    }
}
