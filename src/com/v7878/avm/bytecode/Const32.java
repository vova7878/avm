package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Int32;
import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class Const32 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("const32", new NodeParser.SimpleInstructionCreator((objs) -> new Const32((int) objs[0], (int) objs[1]), Register, Int32));
    }

    private final int A, B;

    public Const32(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putInt(A, B);
    }
}
