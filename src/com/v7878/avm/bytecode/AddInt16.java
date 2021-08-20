package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class AddInt16 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("add-int16", new NodeParser.SimpleInstructionCreator((objs) -> new AddInt16((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public AddInt16(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        short a = data.getShort(A);
        short b = data.getShort(B);
        data.putShort(C, (short) (a + b));
    }
}
