package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import static com.v7878.avm.NodeParser.ParamType.SimpleUInt;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class MoveA extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("moveA", new SimpleInstructionCreator(
                (objs) -> new MoveA((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, SimpleUInt));
    }

    private final int A, B, C;

    public MoveA(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        data.copy(A, B, C);
    }
}
