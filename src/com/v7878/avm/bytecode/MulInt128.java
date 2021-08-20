package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;
import com.v7878.avm.utils.Wide;

public class MulInt128 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("mul-int128", new NodeParser.SimpleInstructionCreator((objs) -> new MulInt128((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public MulInt128(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        Wide a = data.getWide(A);
        Wide b = data.getWide(B);
        data.putWide(C, a.multiply(b));
    }
}
