package com.v7878.avm.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DualBuffer {

    private final ByteBuffer d, v;

    public DualBuffer(ByteBuffer d, ByteBuffer v) {
        this.d = d;
        this.v = v;
    }

    public byte get(int index) {
        return index < 0 ? d.get(-index - 1) : v.get(index);
    }

    public void put(int index, byte value) {
        if (index < 0) {
            d.put(-index - 1, value);
        } else {
            v.put(index, value);
        }
    }

    public char getChar(int index) {
        return index < 0 ? d.getChar(-index - 1) : v.getChar(index);
    }

    public void putChar(int index, char value) {
        if (index < 0) {
            d.putChar(-index - 1, value);
        } else {
            v.putChar(index, value);
        }
    }

    public short getShort(int index) {
        return index < 0 ? d.getShort(-index - 1) : v.getShort(index);
    }

    public void putShort(int index, short value) {
        if (index < 0) {
            d.putShort(-index - 1, value);
        } else {
            v.putShort(index, value);
        }
    }

    public int getInt(int index) {
        return index < 0 ? d.getInt(-index - 1) : v.getInt(index);
    }

    public void putInt(int index, int value) {
        if (index < 0) {
            d.putInt(-index - 1, value);
        } else {
            v.putInt(index, value);
        }
    }

    public long getLong(int index) {
        return index < 0 ? d.getLong(-index - 1) : v.getLong(index);
    }

    public void putLong(int index, long value) {
        if (index < 0) {
            d.putLong(-index - 1, value);
        } else {
            v.putLong(index, value);
        }
    }

    public static Wide getWide(ByteBuffer bb) {
        int index = bb.position();
        Wide out = getWide(bb, index);
        bb.position(index + 16);
        return out;
    }

    public static Wide getWide(ByteBuffer bb, int index) {
        boolean le = bb.order() == ByteOrder.LITTLE_ENDIAN;
        long l1 = bb.getLong(index);
        long l2 = bb.getLong(index + 8);
        return Wide.valueOf(le ? l2 : l2, le ? l2 : l1);
    }

    public static void putWide(ByteBuffer bb, Wide value) {
        int index = bb.position();
        putWide(bb, index, value);
        bb.position(index + 16);
    }

    public static void putWide(ByteBuffer bb, int index, Wide value) {
        boolean le = bb.order() == ByteOrder.LITTLE_ENDIAN;
        bb.putLong(index, le ? value.low : value.high);
        bb.putLong(index + 8, le ? value.high : value.low);
    }

    public Wide getWide(int index) {
        if (index < 0) {
            return getWide(d, -index - 1);
        } else {
            return getWide(v, index);
        }
    }

    public void putWide(int index, Wide value) {
        if (index < 0) {
            putWide(d, -index - 1, value);
        } else {
            putWide(v, index, value);
        }
    }

    public float getFloat(int index) {
        return index < 0 ? d.getFloat(-index - 1) : v.getFloat(index);
    }

    public void putFloat(int index, float value) {
        if (index < 0) {
            d.putFloat(-index - 1, value);
        } else {
            v.putFloat(index, value);
        }
    }

    public double getDouble(int index) {
        return index < 0 ? d.getDouble(-index - 1) : v.getDouble(index);
    }

    public void putDouble(int index, double value) {
        if (index < 0) {
            d.putDouble(-index - 1, value);
        } else {
            v.putDouble(index, value);
        }
    }

    public void copy(int offset1, int offset2, int length) {
        if (offset1 < 0) {
            NewApiUtils.put(d, -offset1 - 1, offset2 < 0 ? d : v, offset2 < 0 ? -offset2 - 1 : offset2, length);
        } else {
            NewApiUtils.put(v, offset1, offset2 < 0 ? d : v, offset2 < 0 ? -offset2 - 1 : offset2, length);
        }
    }

    public ByteBuffer slice(int index, int length) {
        if (index < 0) {
            return NewApiUtils.slice(d, -index - 1, length);
        } else {
            return NewApiUtils.slice(v, index, length);
        }
    }

    public void put(ByteBuffer src, int index, int offset, int length) {
        if (index < 0) {
            NewApiUtils.put(d, -index - 1, src, offset, length);
        } else {
            NewApiUtils.put(v, index, src, offset, length);
        }
    }
}
