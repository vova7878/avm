package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class CmpUInt16 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmp-uint16", new SimpleInstructionCreator(
                (objs) -> new CmpUInt16((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public CmpUInt16(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        int a = data.getShort(A) & 0xffff;
        int b = data.getShort(B) & 0xffff;
        data.put(C, (byte) Integer.compare(a, b));
    }
}
