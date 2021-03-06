package com.v7878.avm.bytecode;

import com.v7878.avm.InvokeRequest;
import com.v7878.avm.Machine;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import static com.v7878.avm.NodeParser.ParamType.SimpleUInt;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;
import java.nio.ByteBuffer;

public class Invoke implements Instruction {

    static void init() {
        NodeParser.addCreator("invoke", new SimpleInstructionCreator(
                (objs) -> new Invoke((int) objs[0], (int) objs[1], (int) objs[2], (int) objs[3], (int) objs[4]),
                Register, Register, SimpleUInt, Register, SimpleUInt));
    }

    private final int A, B, C, D, F;

    public Invoke(int A, int B, int C, int D, int F) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.D = D;
        this.F = F;
    }

    @Override
    public int handle(Node thiz, DualBuffer data, Interpreter inter, InvokeRequest[] req, boolean[] end) {
        Machine m = Machine.get();
        Node node = m.getNode(data.getInt(A));
        req[0] = new InvokeRequest(node, data.slice(B, C), D, F);
        return 1;
    }
}
