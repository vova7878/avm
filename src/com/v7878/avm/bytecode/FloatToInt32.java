package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class FloatToInt32 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("float-to-int32", new SimpleInstructionCreator(
                (objs) -> new FloatToInt32((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public FloatToInt32(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putInt(B, (int) data.getFloat(A));
    }
}
