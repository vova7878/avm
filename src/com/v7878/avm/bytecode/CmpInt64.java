package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class CmpInt64 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmp-int64", new SimpleInstructionCreator(
                (objs) -> new CmpInt64((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public CmpInt64(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        long a = data.getLong(A);
        long b = data.getLong(B);
        data.put(C, (byte) Long.compare(a, b));
    }
}
