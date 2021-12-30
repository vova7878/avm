package com.v7878.avm.utils;

public final class Wide extends Number implements Comparable<Wide> {

    public static final int SIZE = 128;
    public static final int BYTES = SIZE / Byte.SIZE;
    public static final Wide MIN_VALUE = new Wide(0, 0x8000000000000000L);
    public static final Wide MAX_VALUE = new Wide(0xffffffffffffffffL, 0x7fffffffffffffffL);

    public static final Wide ZERO = valueOf(0);
    public static final Wide ONE = valueOf(1);
    public static final Wide TWO = valueOf(2);
    public static final Wide TEN = valueOf(10);

    private static final int LONG_SIZE = 64;

    public final long low, high;

    private Wide(long low, long high) {
        this.low = low;
        this.high = high;
    }

    private Wide(long value) {
        this.low = value;
        this.high = value < 0 ? 0xffffffffffffffffL : 0;
    }

    private Wide(int value) {
        this((long) value);
    }

    public long getHigh() {
        return high;
    }

    public long getLow() {
        return low;
    }

    public Wide increment() {
        long nlow = low + 1;
        return Wide.valueOf(nlow, nlow == 0 ? high + 1 : high);
    }

    public Wide decrement() {
        long nlow = low - 1;
        return Wide.valueOf(nlow, nlow == -1 ? high - 1 : high);
    }

    public Wide negate() {
        return not().increment();
    }

    public Wide add(Wide y) {
        return addUnsignedLow(y.low).addHigh(y.high);
    }

    public Wide substract(Wide y) {
        return add(y.negate());
    }

    private Wide addUnsignedLow(long y) {
        long r = low + y;
        return Wide.valueOf(r, ((low & y) | (low & ~y & ~r) | (~low & y & ~r)) < 0 ? high + 1 : high);
    }

    private Wide addHigh(long y) {
        return Wide.valueOf(low, high + y);
    }

    public Wide not() {
        return Wide.valueOf(~low, ~high);
    }

    public Wide xor(Wide y) {
        return Wide.valueOf(low ^ y.low, high ^ y.high);
    }

    public Wide and(Wide y) {
        return Wide.valueOf(low & y.low, high & y.high);
    }

    public Wide or(Wide y) {
        return Wide.valueOf(low | y.low, high | y.high);
    }

    public Wide leftShift(int shiftDistance) {
        shiftDistance = shiftDistance & 0x7F;
        if (shiftDistance == 0) {
            return this;
        }
        if (shiftDistance < LONG_SIZE) {
            return Wide.valueOf(low << shiftDistance, (low >>> (LONG_SIZE - shiftDistance)) | (high << shiftDistance));
        } else {
            return Wide.valueOf(0, low << (shiftDistance - LONG_SIZE));
        }
    }

    public Wide unsignedRightShift(int shiftDistance) {
        shiftDistance = shiftDistance & 0x7F;
        if (shiftDistance == 0) {
            return this;
        }
        if (shiftDistance < LONG_SIZE) {
            return Wide.valueOf((high << (LONG_SIZE - shiftDistance)) | (low >>> shiftDistance), high >>> shiftDistance);
        } else {
            return Wide.valueOf(high >>> (shiftDistance - LONG_SIZE), 0);
        }
    }

    public Wide signedRightShift(int shiftDistance) {
        shiftDistance = shiftDistance & 0x7F;
        if (shiftDistance == 0) {
            return this;
        }
        if (shiftDistance < LONG_SIZE) {
            return Wide.valueOf((high << (LONG_SIZE - shiftDistance)) | (low >>> shiftDistance), high >> shiftDistance);
        } else {
            return Wide.valueOf(high >> (shiftDistance - LONG_SIZE), high < 0 ? 0xffffffffffffffffL : 0);
        }
    }

    public int numberOfLeadingZeros() {
        return numberOfLeadingZeros(low, high);
    }

    private static int numberOfLeadingZeros(long l, long h) {
        return h == 0 ? Long.numberOfLeadingZeros(l) + 64 : Long.numberOfLeadingZeros(h);
    }

    public int numberOfTrailingZeros() {
        return numberOfTrailingZeros(low, high);
    }

    private static int numberOfTrailingZeros(long l, long h) {
        return l == 0 ? Long.numberOfTrailingZeros(h) + 64 : Long.numberOfTrailingZeros(l);
    }

    public Wide multiply(Wide y) {
        int[] out = new int[4];
        multiply(out, toIntArray(), y.toIntArray(), 4);
        return fromIntArray(out);
    }

    private static void multiply(int[] w, int[] v, int[] u, int s) {
        for (int j = 0; j < s; j++) {
            long k = 0;
            for (int i = 0; i + j < s; i++) {
                long t = (u[i] & 0xffffffffL) * (v[j] & 0xffffffffL) + (w[i + j] & 0xffffffffL) + k;
                w[i + j] = (int) t;
                k = t >>> 32;
            }
        }
    }

    public Wide divide(Wide y) {
        return divide(this, y);
    }

    public static Wide divide(Wide x, Wide y) {
        return divideAndRemainder(x, y)[0];
    }

    public Wide remainder(Wide y) {
        return remainder(this, y);
    }

    public static Wide remainder(Wide x, Wide y) {
        return divideAndRemainder(x, y)[1];
    }

    public Wide[] divideAndRemainder(Wide y) {
        return divideAndRemainder(this, y);
    }

    public static Wide[] divideAndRemainder(Wide x, Wide y) {
        if (y.isZero()) {
            // division by zero
            throw new ArithmeticException();
        }

        boolean yNegative;
        if (yNegative = y.isNegative()) {
            y = y.negate();
        }
        int divisorSize = y.numberOfLeadingZeros();
        boolean xNegative;
        if (xNegative = x.isNegative()) {
            x = x.negate();
        }
        int dividentSize = x.numberOfLeadingZeros();

        Wide out = ZERO;

        // --- setup the maximum number of shifts ---
        int i = divisorSize - dividentSize;
        y = y.leftShift(i);
        for (; i >= 0; i--) {
            Wide tmp = x.substract(y);
            if (!tmp.isNegative()) {
                x = tmp;
                // --- we have subtracted y * 2 ^ n, so include 2 ^ n to the result ---
                if (i >= LONG_SIZE) {
                    out = Wide.valueOf(out.low, out.high | (1L << (i - LONG_SIZE)));
                } else {
                    out = Wide.valueOf(out.low | (1L << i), out.high);
                }
            }
            y = y.unsignedRightShift(1);
        }

        // make sure we return a correctly signed value (may mess up sign bit on overflows?)
        if (xNegative ^ yNegative) {
            out = out.negate();
        }
        if (xNegative) {
            x = x.negate();
        }
        return new Wide[]{out, x};
    }

    public Wide divideUnsigned(Wide y) {
        return divideUnsigned(this, y);
    }

    public static Wide divideUnsigned(Wide x, Wide y) {
        return divideAndRemainderUnsigned(x, y)[0];
    }

    public Wide remainderUnsigned(Wide y) {
        return remainderUnsigned(this, y);
    }

    public static Wide remainderUnsigned(Wide x, Wide y) {
        return divideAndRemainderUnsigned(x, y)[1];
    }

    public Wide[] divideAndRemainderUnsigned(Wide y) {
        return divideAndRemainderUnsigned(this, y);
    }

    public static Wide[] divideAndRemainderUnsigned(Wide x, Wide y) {
        if (y.isZero()) {
            // division by zero
            throw new ArithmeticException();
        }

        int divisorSize = y.numberOfLeadingZeros();
        int dividentSize = x.numberOfLeadingZeros();

        Wide out = ZERO;

        // --- setup the maximum number of shifts ---
        int i = divisorSize - dividentSize;
        y = y.leftShift(i);
        for (; i >= 0; i--) {
            Wide tmp = x.substract(y);
            if (!tmp.isNegative()) {
                x = tmp;
                // --- we have subtracted y * 2 ^ n, so include 2 ^ n to the result ---
                if (i >= LONG_SIZE) {
                    out = Wide.valueOf(out.low, out.high | (1L << (i - LONG_SIZE)));
                } else {
                    out = Wide.valueOf(out.low | (1L << i), out.high);
                }
            }
            y = y.unsignedRightShift(1);
        }

        return new Wide[]{out, x};
    }

    public boolean isZero() {
        return low == 0 && high == 0;
    }

    public boolean isNegative() {
        return high < 0;
    }

    @Override
    public int intValue() {
        return (int) low;
    }

    @Override
    public long longValue() {
        return low;
    }

    private static double toDouble(long unsigned) {
        if (unsigned < 0) {
            return (double) (unsigned & Long.MAX_VALUE) + 9.223372036854776e18d;
        }
        return unsigned;
    }

    private static float toFloat(long unsigned) {
        if (unsigned < 0) {
            return (float) (unsigned & Long.MAX_VALUE) + 9.223372e18f;
        }
        return unsigned;
    }

    @Override
    public double doubleValue() {
        return high < 0 ? -negate().unsignedDoubleValue() : unsignedDoubleValue();
    }

    @Override
    public float floatValue() {
        return high < 0 ? -negate().unsignedFloatValue() : unsignedFloatValue();
    }

    public double unsignedDoubleValue() {
        return Math.scalb(toDouble(high), LONG_SIZE) + toDouble(low);
    }

    public float unsignedFloatValue() {
        return Math.scalb(toFloat(high), LONG_SIZE) + toFloat(low);
    }

    private int[] toIntArray() {
        return new int[]{(int) low, (int) (low >>> 32), (int) high, (int) (high >>> 32)};
    }

    private static Wide fromIntArray(int[] data) {
        return Wide.valueOf((data[0] & 0xffffffffL) | ((long) data[1] << 32), (data[2] & 0xffffffffL) | ((long) data[3] << 32));
    }

    @Override
    public int compareTo(Wide obj) {
        return compare(this, obj);
    }

    public static int compare(Wide x, Wide y) {
        if (x.high > y.high) {
            return 1;
        }
        if (x.high < y.high) {
            return -1;
        }
        return NewApiUtils.compareUnsigned(x.low, y.low);
    }

    public int compareToUnsigned(Wide obj) {
        return compareUnsigned(this, obj);
    }

    public static int compareUnsigned(Wide x, Wide y) {
        int tmp = NewApiUtils.compareUnsigned(x.high, y.high);
        if (tmp != 0) {
            return tmp;
        }
        return NewApiUtils.compareUnsigned(x.low, y.low);
    }

    public int signum() {
        return signum(this);
    }

    public static int signum(Wide x) {
        if (x.high > 0) {
            return 1;
        }
        if (x.high < 0) {
            return -1;
        }
        return NewApiUtils.compareUnsigned(x.low, 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Wide)) {
            return false;
        }
        Wide other = (Wide) obj;
        return (low == other.low)
                && (high == other.high);
    }

    @Override
    public int hashCode() {
        return (int) (low ^ high ^ (low >> 32) ^ (high >> 32));
    }

    @Override
    public String toString() {
        return toString(this, 10);
    }

    public String toHexString() {
        return toHexString(this);
    }

    public static String toHexString(Wide i) {
        return toString(i, 16);
    }

    public String toBinaryString() {
        return toBinaryString(this);
    }

    public static String toBinaryString(Wide i) {
        return toString(i, 2);
    }

    private static final char[] digits = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    };

    public String toString(int radix) {
        return toString(this, radix);
    }

    public static String toString(Wide i, int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            radix = 10;
        }
        char[] buf = new char[129];
        int charPos = 128;
        boolean negative = i.isNegative();

        if (!negative) {
            i = i.negate();
        }

        Wide wradix = Wide.valueOf(radix);
        Wide wnradix = Wide.valueOf(-radix);
        while (Wide.compare(i, wnradix) <= 0) {
            Wide[] r = i.divideAndRemainder(wradix);
            buf[charPos--] = digits[r[1].negate().intValue()];
            i = r[0];
        }
        buf[charPos] = digits[i.negate().intValue()];

        if (negative) {
            buf[--charPos] = '-';
        }
        return String.valueOf(buf, charPos, (129 - charPos));
    }

    private static class WideCache {

        private WideCache() {
        }

        static final int UP = 511, DOWN = -512;
        static final Wide[] cache;

        static {
            int size = UP - DOWN + 1;
            Wide[] c = new Wide[size];
            long value = DOWN;
            for (int i = 0; i < size; i++) {
                c[i] = new Wide(value++);
            }
            cache = c;
        }
    }

    public static Wide valueOf(long l, long h) {
        if ((h == 0 && l >= 0) || (h == -1 && l < 0)) {
            return valueOf(l);
        }
        return new Wide(l, h);
    }

    public static Wide valueOf(long l) {
        final int offset = -WideCache.DOWN;
        if (l >= WideCache.DOWN && l <= WideCache.UP) {
            return WideCache.cache[(int) l + offset];
        }
        return new Wide(l);
    }

    public static Wide valueOf(int l) {
        final int offset = -WideCache.DOWN;
        if (l >= WideCache.DOWN && l <= WideCache.UP) {
            return WideCache.cache[l + offset];
        }
        return new Wide(l);
    }

    public static Wide valueOf(String s) {
        return valueOf(s, 10);
    }

    public static Wide valueOf(String s, int radix)
            throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix
                    + " less than Character.MIN_RADIX");
        }
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix
                    + " greater than Character.MAX_RADIX");
        }

        boolean negative = false;
        int i = 0, len = s.length();
        Wide limit = Wide.MAX_VALUE.negate();

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Wide.MIN_VALUE;
                } else if (firstChar != '+') {
                    throw forInputString(s, radix);
                }

                if (len == 1) { // Cannot have lone "+" or "-"
                    throw forInputString(s, radix);
                }
                i++;
            }
            Wide wradix = Wide.valueOf(radix);
            Wide multmin = limit.divide(wradix);
            Wide result = ZERO;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                Wide digit = Wide.valueOf(Character.digit(s.charAt(i++), radix));
                if (digit.isNegative() || Wide.compare(result, multmin) < 0) {
                    throw forInputString(s, radix);
                }
                result = result.multiply(wradix);
                if (Wide.compare(result, limit.add(digit)) < 0) {
                    throw forInputString(s, radix);
                }
                result = result.substract(digit);
            }
            return negative ? result : result.negate();
        } else {
            throw forInputString(s, radix);
        }
    }

    private static NumberFormatException forInputString(String s, int radix) {
        return new NumberFormatException("For input string: \"" + s + "\""
                + (radix == 10 ? "" : " under radix " + radix));
    }
}
