package com.v7878.avm.bytecode;

import static com.v7878.avm.NodeParser.ParamType.Register;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class CmpzInt8 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("cmpz-int8", new NodeParser.SimpleInstructionCreator((objs) -> new CmpzInt8((int) objs[0], (int) objs[1]), Register, Register));
    }

    private final int A, B;

    public CmpzInt8(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        byte a = data.get(A);
        data.put(B, (byte) Byte.compare(a, (byte) 0));
    }
}
