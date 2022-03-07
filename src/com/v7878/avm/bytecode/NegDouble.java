package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class NegDouble extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("neg-double", new SimpleInstructionCreator(
                (objs) -> new NegDouble((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public NegDouble(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        double a = data.getDouble(A);
        data.putDouble(B, -a);
    }
}
