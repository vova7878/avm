package com.v7878.avm.bytecode;

import com.v7878.avm.Machine;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import static com.v7878.avm.NodeParser.ParamType.String;
import com.v7878.avm.utils.DualBuffer;

public class FindNode extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("find-node", new NodeParser.SimpleInstructionCreator(
                (objs) -> new FindNode((int) objs[0], (String) objs[1]),
                Register, String));
    }

    private final int A;
    private final String B;

    public FindNode(int A, String B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        data.putInt(A, Machine.get().findNode(B).getIndex());
    }
}
