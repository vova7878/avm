package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class CmpInt16 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmp-int16", new NodeParser.SimpleInstructionCreator((objs) -> new CmpInt16((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public CmpInt16(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        short a = data.getShort(A);
        short b = data.getShort(B);
        data.put(C, (byte) Short.compare(a, b));
    }
}
