package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class SubFloat extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("sub-float", new SimpleInstructionCreator(
                (objs) -> new SubFloat((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public SubFloat(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        float a = data.getFloat(A);
        float b = data.getFloat(B);
        data.putFloat(C, a - b);
    }
}
