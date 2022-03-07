package com.v7878.avm.bytecode;

import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class GetThis implements Instruction {

    static void init() {
        NodeParser.addCreator("get-this", new SimpleInstructionCreator(
                (objs) -> new GetThis((int) objs[0]),
                Register));
    }

    private final int A;

    public GetThis(int A) {
        this.A = A;
    }

    @Override
    public int handle(Node node, DualBuffer data, Interpreter inter, boolean[] end) {
        data.putInt(A, node.getIndex());
        return 1;
    }
}
