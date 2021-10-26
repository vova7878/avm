package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.utils.DualBuffer;

public class UInt32ToFloat extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("uint32-to-float", new NodeParser.SimpleInstructionCreator(
                (objs) -> new UInt32ToFloat((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public UInt32ToFloat(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putFloat(B, data.getInt(A) & 0xffffffffL);
    }
}