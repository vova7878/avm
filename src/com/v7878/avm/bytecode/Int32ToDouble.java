package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class Int32ToDouble extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("int32-to-double", new NodeParser.SimpleInstructionCreator(
                (objs) -> new Int32ToDouble((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public Int32ToDouble(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putDouble(B, data.getInt(A));
    }
}
