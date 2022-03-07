package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class CmpzFloat extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmpz-float", new SimpleInstructionCreator(
                (objs) -> new CmpzFloat((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public CmpzFloat(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        float a = data.getFloat(A);
        data.put(B, (byte) Float.compare(a, 0));
    }
}
