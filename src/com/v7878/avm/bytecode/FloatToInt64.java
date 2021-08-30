package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.utils.DualBuffer;

public class FloatToInt64 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("float-to-int64", new NodeParser.SimpleInstructionCreator(
                (objs) -> new FloatToInt64((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public FloatToInt64(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putLong(B, (long) data.getFloat(A));
    }
}
