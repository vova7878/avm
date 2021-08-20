package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class AddFloat extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("add-float", new NodeParser.SimpleInstructionCreator((objs) -> new AddFloat((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public AddFloat(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        float a = data.getFloat(A);
        float b = data.getFloat(B);
        data.putFloat(C, a + b);
    }
}
