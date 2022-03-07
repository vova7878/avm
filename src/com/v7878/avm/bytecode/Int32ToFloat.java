package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class Int32ToFloat extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("int32-to-float", new SimpleInstructionCreator(
                (objs) -> new Int32ToFloat((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public Int32ToFloat(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putFloat(B, data.getInt(A));
    }
}
