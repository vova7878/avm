package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class DoubleToFloat extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("double-to-float", new SimpleInstructionCreator(
                (objs) -> new DoubleToFloat((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public DoubleToFloat(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putFloat(B, (float) data.getDouble(A));
    }
}
