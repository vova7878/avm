package com.v7878.avm.bytecode;

import com.v7878.avm.Machine;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class GetData16 extends DataInstruction {

    static void init() {
        NodeParser.addCreator("get-data16", new SimpleInstructionCreator(
                (objs) -> new GetData16((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public GetData16(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(Node thiz, DualBuffer data) {
        Machine m = Machine.get();
        Node node = m.getNode(data.getInt(B));
        Utils.checkPrivate(thiz, node);
        data.putShort(A, node.getData().getShort(data.getInt(C)));
    }
}
