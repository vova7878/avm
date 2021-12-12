package com.v7878.avm;

public class Metadata {

    public final int index;
    public final InvokeInfo iinfo;
    public volatile int flags;

    public Metadata(int index, InvokeInfo iinfo, int flags) {
        this.index = index;
        this.iinfo = iinfo;
        this.flags = flags;
    }

    public static class InvokeInfo {

        public final int regs;
        public final int ins;
        public final int outs;
        public final NodeHandler h;
        public volatile int invocationCount;

        public InvokeInfo(int regs, int ins, int outs, NodeHandler h) {
            this.regs = regs;
            this.ins = ins;
            this.outs = outs;
            this.h = h;
        }
    }
}
