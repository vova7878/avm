package com.v7878.avm.bytecode;

import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Identifier;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class IfGez implements Instruction {

    static void init() {
        NodeParser.addCreator("if-gez", new SimpleInstructionCreator(
                (objs) -> new IfGez((int) objs[0], (int) objs[1]),
                Register, Identifier));
    }

    private final int A, B;

    public IfGez(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public int handle(Node node, DualBuffer data, Interpreter inter, boolean[] end) {
        byte a = data.get(A);
        return a >= 0 ? B : 1;
    }
}
