package com.v7878.avm.bytecode;

import com.v7878.avm.Machine;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class Delete extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("delete", new SimpleInstructionCreator(
                (objs) -> new Delete((int) objs[0]),
                Register));
    }

    private final int A;

    public Delete(int A) {
        this.A = A;
    }

    @Override
    public void handle(DualBuffer data) {
        Machine m = Machine.get();
        Node node = m.getNode(data.getInt(A));
        m.deleteNode(node);
    }
}
