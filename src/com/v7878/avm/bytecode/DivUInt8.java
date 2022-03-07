package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class DivUInt8 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("div-uint8", new SimpleInstructionCreator(
                (objs) -> new DivUInt8((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public DivUInt8(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        int a = data.get(A) & 0xff;
        int b = data.get(B) & 0xff;
        data.put(C, (byte) (a / b));
    }
}
