package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class OrInt64 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("or-int64", new NodeParser.SimpleInstructionCreator((objs) -> new OrInt64((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public OrInt64(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        long a = data.getLong(A);
        long b = data.getLong(B);
        data.putLong(C, a | b);
    }
}
