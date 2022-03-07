package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;
import com.v7878.avm.utils.Wide;

public class CmpzUInt128 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmpz-uint128", new SimpleInstructionCreator(
                (objs) -> new CmpzUInt128((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public CmpzUInt128(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        Wide a = data.getWide(A);
        data.put(B, (byte) (a.isZero() ? 0 : 1));
    }
}
