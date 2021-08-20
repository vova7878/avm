package com.v7878.avm.bytecode;

import com.v7878.avm.Node;
import com.v7878.avm.utils.DualBuffer;

public interface Instruction {

    int handle(Node node, DualBuffer data, Interpreter inter, boolean[] end);
}
