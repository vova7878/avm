package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;
import com.v7878.avm.utils.Wide;

public class Int16ToInt128 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("int16-to-int128", new SimpleInstructionCreator(
                (objs) -> new Int16ToInt128((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public Int16ToInt128(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putWide(B, Wide.valueOf(data.getShort(A)));
    }
}
