package com.v7878.avm;

import com.v7878.avm.threads.StackElement;

@FunctionalInterface
public interface NodeHandler {

    InvokeRequest handle(StackElement current);
}
