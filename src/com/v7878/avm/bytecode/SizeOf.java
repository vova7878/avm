package com.v7878.avm.bytecode;

import com.v7878.avm.Machine;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import static com.v7878.avm.NodeParser.ParamType.Register;
import com.v7878.avm.utils.DualBuffer;

public class SizeOf extends SimpleInstruction {

    static void init() {
        NodeParser.addCreator("size-of", new NodeParser.SimpleInstructionCreator(
                (objs) -> new SizeOf((int) objs[0], (int) objs[1]),
                Register, Register));
    }

    private final int A, B;

    public SizeOf(int A, int B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public void handle(DualBuffer data) {
        Machine m = Machine.get();
        Node node = m.getNode(data.getInt(A));
        data.putInt(B, node.size());
    }
}
