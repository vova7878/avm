package com.v7878.avm.bytecode;

import com.v7878.avm.Machine;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import static com.v7878.avm.NodeParser.ParamType.SimpleUInt;
import com.v7878.avm.utils.DualBuffer;

public class GetData64 extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("get-data64", new NodeParser.SimpleInstructionCreator(
                (objs) -> new GetData64((int) objs[0], (int) objs[1], (int) objs[2]),
                Register, Register, Register));
    }

    private final int A, B, C;

    public GetData64(int A, int B, int C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }

    @Override
    public void handle(DualBuffer data) {
        Machine m = Machine.get();
        Node node = m.getNode(data.getInt(B));
        data.putLong(A, node.getData().getLong(data.getInt(C)));
    }
}
