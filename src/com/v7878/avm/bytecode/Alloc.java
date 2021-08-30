package com.v7878.avm.bytecode;

import com.v7878.avm.Machine;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.utils.DualBuffer;

public class Alloc extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("alloc", new NodeParser.SimpleInstructionCreator(
                (objs) -> new Alloc((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public Alloc(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        Machine m = Machine.get();
        Node node = m.newNode(data.getInt(B));
        data.putInt(A, node.getIndex());
    }
}
