package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class CmpDouble extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmp-double", new NodeParser.SimpleInstructionCreator((objs) -> new CmpDouble((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public CmpDouble(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        double a = data.getDouble(A);
        double b = data.getDouble(B);
        data.put(C, (byte) Double.compare(a, b));
    }
}
