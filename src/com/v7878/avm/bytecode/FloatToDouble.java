package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.utils.DualBuffer;

public class FloatToDouble extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("float-to-double", new NodeParser.SimpleInstructionCreator(
                (objs) -> new FloatToDouble((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public FloatToDouble(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putDouble(B, data.getFloat(A));
    }
}