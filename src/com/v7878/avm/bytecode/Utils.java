package com.v7878.avm.bytecode;

import static com.v7878.avm.Constants.NODE_PRIVATE;
import static com.v7878.avm.Constants.NODE_PROTECTED;
import com.v7878.avm.Node;

public class Utils {

    public static void checkProtected(Node thiz, Node node) {
        if (node != thiz && node.withFlags(NODE_PROTECTED)) {
            throw new IllegalStateException("Trying to change protected node");
        }
    }

    public static void checkPrivate(Node thiz, Node node) {
        if (node != thiz && node.withFlags(NODE_PRIVATE)) {
            throw new IllegalStateException("Trying to read private node");
        }
    }
}
