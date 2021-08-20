package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class AddInt8 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("add-int8", new NodeParser.SimpleInstructionCreator((objs) -> new AddInt8((int) objs[0], (int) objs[1], (int) objs[2]), Register, Register, Register));
    }

    private final int A, B, C;

    public AddInt8(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        byte a = data.get(A);
        byte b = data.get(B);
        data.put(C, (byte) (a + b));
    }
}
