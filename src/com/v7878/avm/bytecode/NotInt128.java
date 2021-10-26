package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;
import com.v7878.avm.utils.Wide;

public class NotInt128 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("not-int128", new NodeParser.SimpleInstructionCreator(
                (objs) -> new NotInt128((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public NotInt128(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        Wide a = data.getWide(A);
        data.putWide(B, a.not());
    }
}
