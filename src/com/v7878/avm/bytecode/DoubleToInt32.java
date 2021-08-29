package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.utils.DualBuffer;

public class DoubleToInt32 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("double-to-int32", new NodeParser.SimpleInstructionCreator(
                (objs) -> new DoubleToInt32((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public DoubleToInt32(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putInt(B, (int) data.getDouble(A));
    }
}
