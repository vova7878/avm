package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class CmpzDouble extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmpz-double", new SimpleInstructionCreator(
                (objs) -> new CmpzDouble((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public CmpzDouble(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        double a = data.getDouble(A);
        data.put(B, (byte) Double.compare(a, 0));
    }
}
