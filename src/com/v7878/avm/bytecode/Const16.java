package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Int16;
import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class Const16 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("const16", new NodeParser.SimpleInstructionCreator(
                (objs) -> new Const16((int) objs[0], (short) objs[1]),
                Register, Int16));
    }

    private final int A;
    private final short B;

    public Const16(int A, short B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putShort(A, B);
    }
}
