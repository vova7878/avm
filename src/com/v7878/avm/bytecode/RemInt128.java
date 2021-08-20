package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;
import com.v7878.avm.utils.Wide;

public class RemInt128 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("rem-int128", new NodeParser.SimpleInstructionCreator((objs) -> new RemInt128((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public RemInt128(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        Wide a = data.getWide(A);
        Wide b = data.getWide(B);
        data.putWide(C, a.remainder(b));
    }
}
