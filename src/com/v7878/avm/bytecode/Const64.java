package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Int64;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class Const64 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("const64", new SimpleInstructionCreator(
                (objs) -> new Const64((int) objs[0], (long) objs[1]),
                Register, Int64));
    }

    private final int A;
    private final long B;

    public Const64(int A, long B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putLong(A, B);
    }
}
