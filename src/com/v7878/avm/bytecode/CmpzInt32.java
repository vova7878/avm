package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class CmpzInt32 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmpz-int32", new NodeParser.SimpleInstructionCreator((objs) -> new CmpzInt32((int) objs[0], (int) objs[1]), Register, Register));
    }

    private final int A, B;

    public CmpzInt32(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        int a = data.getInt(A);
        data.put(B, (byte) Integer.compare(a, 0));
    }
}
