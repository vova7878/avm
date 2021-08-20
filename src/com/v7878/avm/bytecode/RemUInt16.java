package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class RemUInt16 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("rem-uint16", new NodeParser.SimpleInstructionCreator((objs) -> new RemUInt16((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public RemUInt16(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        int a = data.getShort(A) & 0xffff;
        int b = data.getShort(B) & 0xffff;
        data.putShort(C, (short) (a % b));
    }
}
