package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.utils.DualBuffer;

public class UInt64ToFloat extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("uint64-to-float", new NodeParser.SimpleInstructionCreator(
                (objs) -> new UInt64ToFloat((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public UInt64ToFloat(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putFloat(B, toFloat(data.getLong(A)));
    }

    private static float toFloat(long unsigned) {
        if (unsigned < 0) {
            return (float) (unsigned & Long.MAX_VALUE) + 9.223372e18f;
        }
        return unsigned;
    }
}
