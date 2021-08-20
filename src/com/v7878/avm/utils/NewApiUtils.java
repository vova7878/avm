package com.v7878.avm.utils;

import java.nio.ByteBuffer;
import java.util.Map;

public class NewApiUtils {

    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
        V v = map.get(key);
        if (v == null) {
            v = map.put(key, value);
        }
        return v;
    }

    public static int parseInt(CharSequence s, int beginIndex, int endIndex, int radix) {
        return Integer.parseInt(s.subSequence(beginIndex, endIndex).toString(), radix);
    }

    public static void checkFromIndexSize(int fromIndex, int size, int length) {
        if ((length | fromIndex | size) < 0 || size > length - fromIndex) {
            throw new IndexOutOfBoundsException(
                    String.format("Range [%d, %<d + %d) out of bounds for length %d",
                            fromIndex, size, length));
        }
    }

    public static void put(ByteBuffer dst, int dstPos, ByteBuffer src, int srcPos, int length) {
        //checkFromIndexSize(dstPos, length, dst.limit());
        //checkFromIndexSize(srcPos, length, src.limit());
        src = (ByteBuffer) src.duplicate().position(srcPos).limit(srcPos + length);
        dst = (ByteBuffer) dst.duplicate().position(dstPos).limit(dstPos + length);
        dst.put(src);
    }

    public static ByteBuffer slice(ByteBuffer bb, int index, int length) {
        bb = bb.duplicate();
        bb.position(index).limit(index + length);
        return bb.slice();
    }

    public static int compareUnsigned(long x, long y) {
        return Long.compare(x + Long.MIN_VALUE, y + Long.MIN_VALUE);
    }

    public static long divideUnsigned(long dividend, long divisor) {
        /* See Hacker's Delight (2nd ed), section 9.3 */
        if (divisor >= 0) {
            final long q = (dividend >>> 1) / divisor << 1;
            final long r = dividend - q * divisor;
            return q + ((r | ~(r - divisor)) >>> (Long.SIZE - 1));
        }
        return (dividend & ~(dividend - divisor)) >>> (Long.SIZE - 1);
    }

    public static long remainderUnsigned(long dividend, long divisor) {
        /* See Hacker's Delight (2nd ed), section 9.3 */
        if (divisor >= 0) {
            final long q = (dividend >>> 1) / divisor << 1;
            final long r = dividend - q * divisor;
            return r - ((~(r - divisor) >> (Long.SIZE - 1)) & divisor);
        }
        return dividend - (((dividend & ~(dividend - divisor)) >> (Long.SIZE - 1)) & divisor);
    }
}
