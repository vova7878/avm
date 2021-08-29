package com.v7878.avm.bytecode;

import com.v7878.avm.NodeParser;
import com.v7878.avm.utils.DualBuffer;

public class Nop extends SimpleInstruction {

    public static final Nop INSTANCE = new Nop();

    static void init() {
        NodeParser.addCreator("nop", new NodeParser.SimpleInstructionCreator(
                (objs) -> INSTANCE));
    }

    private Nop() {
    }

    @Override
    public void handle(DualBuffer data) {
    }
}
