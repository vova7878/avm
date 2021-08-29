package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.utils.DualBuffer;

public class Int128ToFloat extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("int128-to-float", new NodeParser.SimpleInstructionCreator(
                (objs) -> new Int128ToFloat((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public Int128ToFloat(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putFloat(B, data.getWide(A).floatValue());
    }
}
