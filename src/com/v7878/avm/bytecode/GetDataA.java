package com.v7878.avm.bytecode;

import com.v7878.avm.Machine;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import static com.v7878.avm.NodeParser.ParamType.SimpleUInt;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class GetDataA extends DataInstruction {

    static void init() {
        NodeParser.addCreator("get-dataA", new SimpleInstructionCreator(
                (objs) -> new GetDataA((int) objs[0], (int) objs[1], (int) objs[2], (int) objs[3]),
                Register, Register, Register, SimpleUInt));
    }

    private final int A, B, C, D;

    public GetDataA(int A, int B, int C, int D) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.D = D;
    }

    @Override
    public void handle(Node thiz, DualBuffer data) {
        Machine m = Machine.get();
        Node node = m.getNode(data.getInt(B));
        Utils.checkPrivate(thiz, node);
        data.put(node.getData(), A, data.getInt(C), D);
    }
}
