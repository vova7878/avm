package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class NegInt64 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("neg-int64", new SimpleInstructionCreator(
                (objs) -> new NegInt64((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public NegInt64(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        long a = data.getLong(A);
        data.putLong(B, -a);
    }
}
