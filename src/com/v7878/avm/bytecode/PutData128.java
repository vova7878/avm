package com.v7878.avm.bytecode;

import com.v7878.avm.Machine;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class PutData128 extends DataInstruction {

    static void init() {
        NodeParser.addCreator("put-data128", new SimpleInstructionCreator(
                (objs) -> new PutData128((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public PutData128(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(Node thiz, DualBuffer data) {
        Machine m = Machine.get();
        Node node = m.getNode(data.getInt(B));
        Utils.checkProtected(thiz, node);
        DualBuffer.putWide(node.getData(), data.getInt(C), data.getWide(A));
    }
}
