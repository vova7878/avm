package com.v7878.avm;

import java.nio.ByteOrder;

public class Constants {

    public static final ByteOrder ENDIAN = ByteOrder.LITTLE_ENDIAN;
    public static final int NODE_DELETED = 0b1;
    public static final int NODE_INDELIBLE = 0b10;
    public static final int NODE_NAMED = 0b100;
    public static final int NODE_FINAL = 0b1000;
    public static final int NODE_PRIVATE = 0b10000;
}
