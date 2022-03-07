package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class UInt64ToDouble extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("uint64-to-double", new SimpleInstructionCreator(
                (objs) -> new UInt64ToDouble((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public UInt64ToDouble(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putDouble(B, toDouble(data.getLong(A)));
    }

    private static double toDouble(long unsigned) {
        if (unsigned < 0) {
            return (double) (unsigned & Long.MAX_VALUE) + 9.223372036854776e18d;
        }
        return unsigned;
    }
}
