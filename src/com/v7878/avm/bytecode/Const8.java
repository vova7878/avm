package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Int8;
import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class Const8 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("const8", new NodeParser.SimpleInstructionCreator((objs) -> new Const8((int) objs[0], (byte) objs[1]), Register, Int8));
    }

    private final int A;
    private final byte B;

    public Const8(int A, byte B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.put(A, B);
    }
}
