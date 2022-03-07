package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class DivUInt32 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("div-uint32", new SimpleInstructionCreator(
                (objs) -> new DivUInt32((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public DivUInt32(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        long a = data.getInt(A) & 0xffffffffL;
        long b = data.getInt(B) & 0xffffffffL;
        data.putInt(C, (int) (a / b));
    }
}
