package com.v7878.avm.bytecode;

import com.v7878.avm.InvokeRequest;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Identifier;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class IfLez implements Instruction {

    static void init() {
        NodeParser.addCreator("if-lez", new SimpleInstructionCreator(
                (objs) -> new IfLez((int) objs[0], (int) objs[1]),
                Register, Identifier));
    }

    private final int A, B;

    public IfLez(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public int handle(Node node, DualBuffer data, Interpreter inter, InvokeRequest[] req, boolean[] end) {
        byte a = data.get(A);
        return a <= 0 ? B : 1;
    }
}
