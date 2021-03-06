package com.v7878.avm.bytecode;

import com.v7878.avm.InvokeRequest;
import com.v7878.avm.Node;
import com.v7878.avm.NodeParser;
import com.v7878.avm.NodeParser.SimpleInstructionCreator;
import com.v7878.avm.utils.DualBuffer;

public class Return implements Instruction {

    public static final Return INSTANCE = new Return();

    static void init() {
        NodeParser.addCreator("return", new SimpleInstructionCreator(
                (objs) -> INSTANCE));
    }

    private Return() {
    }

    @Override
    public int handle(Node node, DualBuffer data, Interpreter inter, InvokeRequest[] req, boolean[] end) {
        end[0] = true;
        return 0;
    }
}
