package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class CmpUInt32 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmp-uint32", new NodeParser.SimpleInstructionCreator((objs) -> new CmpUInt32((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public CmpUInt32(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        long a = data.getInt(A) & 0xffffffffL;
        long b = data.getInt(B) & 0xffffffffL;
        data.put(C, (byte) Long.compare(a, b));
    }
}
