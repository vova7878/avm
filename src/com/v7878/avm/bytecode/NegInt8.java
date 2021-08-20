package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class NegInt8 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("neg-int8", new NodeParser.SimpleInstructionCreator((objs) -> new NegInt8((int) objs[0], (int) objs[1]), Register, Register));
    }

    private final int A, B;

    public NegInt8(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        int a = data.get(A);
        data.put(B, (byte) -a);
    }
}
