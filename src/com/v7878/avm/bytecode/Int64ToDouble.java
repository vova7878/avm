package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.utils.DualBuffer;

public class Int64ToDouble extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("int64-to-double", new NodeParser.SimpleInstructionCreator(
                (objs) -> new Int64ToDouble((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public Int64ToDouble(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putDouble(B, data.getLong(A));
    }
}
