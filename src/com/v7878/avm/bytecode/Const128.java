package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Int128;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;
import com.v7878.avm.utils.Wide;

public class Const128 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("const128", new SimpleInstructionCreator(
                (objs) -> new Const128((int) objs[0], (Wide) objs[1]),
                Register, Int128));
    }

    private final int A;
    private final Wide B;

    public Const128(int A, Wide B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putWide(A, B);
    }
}
