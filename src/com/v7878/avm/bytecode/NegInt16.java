package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class NegInt16 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("neg-int16", new SimpleInstructionCreator(
                (objs) -> new NegInt16((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public NegInt16(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        int a = data.getShort(A);
        data.putShort(B, (short) -a);
    }
}
