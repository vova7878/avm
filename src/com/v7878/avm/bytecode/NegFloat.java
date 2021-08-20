package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class NegFloat extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("neg-float", new NodeParser.SimpleInstructionCreator((objs) -> new NegFloat((int) objs[0], (int) objs[1]), Register, Register));
    }

    private final int A, B;

    public NegFloat(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        float a = data.getFloat(A);
        data.putFloat(B, -a);
    }
}
