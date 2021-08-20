package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;
import com.v7878.avm.utils.Wide;

public class CmpzInt128 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmpz-int128", new NodeParser.SimpleInstructionCreator(
                (objs) -> new CmpzInt128((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public CmpzInt128(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        Wide a = data.getWide(A);
        data.put(B, (byte) a.signum());
    }
}
