package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class Move8 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("move8", new SimpleInstructionCreator(
                (objs) -> new Move8((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public Move8(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.put(B, data.get(A));
    }
}
