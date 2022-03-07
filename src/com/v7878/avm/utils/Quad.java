package com.v7878.avm.utils;

import static com.v7878.avm.utils.Wide.ZERO;

public final class Quad implements Comparable<Quad>, Comparable2<Quad> {

    public final long low, high;

    public static final Quad POSITIVE_INFINITY = new Quad(0, 0x7fff000000000000L);
    public static final Quad NEGATIVE_INFINITY = new Quad(0, 0xffff000000000000L);
    public static final Quad POSITIVE_ZERO = new Quad(0, 0);
    public static final Quad NEGATIVE_ZERO = new Quad(0, 0x8000000000000000L);
    public static final Quad NaN = new Quad(0, 0x7fff800000000000L);
    public static final Quad MAX_VALUE = new Quad(-1, 0x7ffeffffffffffffL);
    public static final Quad MAX_SUBNORMAL = new Quad(-1, 0x0000ffffffffffffL);
    public static final Quad MIN_NORMAL = new Quad(0, 0x0001000000000000L);
    public static final Quad MIN_VALUE = new Quad(1, 0);

    public static final Quad ONE = new Quad(0, 0x3fff000000000000L);
    public static final Quad TWO = new Quad(0, 0x4000000000000000L);
    public static final Quad HALF = new Quad(0, 0x3ffe000000000000L);

    public static final int MAX_EXPONENT = 16383;
    public static final int MIN_EXPONENT = -16382;

    private static final int EXPONENT_MASK = 0x7fff;
    private static final Wide VALUE_MASK = Wide.valueOf(-1, 0xffffffffffffL);
    private static final int EXPONENT_BIAS = 16383;
    private static final int SIGNIFICANT_BITS = 112;

    private Quad(long low, long high) {
        this.low = low;
        this.high = high;
    }

    public static int getSign(Quad v) {
        return v.high < 0 ? 1 : 0;
    }

    public int getSign() {
        return getSign(this);
    }

    public static boolean isNegative(Quad v) {
        return v.high < 0;
    }

    public boolean isNegative() {
        return isNegative(this);
    }

    public static int getBiasedExponent(Quad v) {
        return (int) (v.high >>> 48) & EXPONENT_MASK;
    }

    public int getBiasedExponent() {
        return getBiasedExponent(this);
    }

    public static Wide getValue(Quad v) {
        return Wide.valueOf(v.low, v.high & 0xffffffffffffL);
    }

    public Wide getValue() {
        return getValue(this);
    }

    public static boolean isNaN(Quad v) {
        return v.getBiasedExponent() == EXPONENT_MASK && !v.getValue().isZero();
    }

    public boolean isNaN() {
        return isNaN(this);
    }

    public static boolean isInfinite(Quad v) {
        return v.getBiasedExponent() == EXPONENT_MASK && v.getValue().isZero();
    }

    public boolean isInfinite() {
        return isInfinite(this);
    }

    public static boolean isFinite(Quad v) {
        return v.getBiasedExponent() != EXPONENT_MASK;
    }

    public boolean isFinite() {
        return isFinite(this);
    }

    public static boolean isZero(Quad v) {
        return v.low == 0 && (v.high == 0 || v.high == 0x8000000000000000L);
    }

    public boolean isZero() {
        return isZero(this);
    }

    public static boolean isSubNormal(Quad v) {
        return v.getBiasedExponent() == 0 && !v.isZero();
    }

    public boolean isSubNormal() {
        return isSubNormal(this);
    }

    public Quad negate() {
        return Quad.valueOf(low, high ^ 0x8000000000000000L);
    }

    private static Quad normalize(Wide v, int exp, boolean neg) {
        int lz = v.numberOfLeadingZeros();
        if (lz == 128) {
            return neg ? NEGATIVE_ZERO : POSITIVE_ZERO;
        }
        int e = exp - lz + 15;
        if (e >= EXPONENT_MASK) {
            return neg ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
        }
        if (e < 0) {
            e = 0;
        }
        int ls = exp - e - (e == 0 ? 1 : 0);
        if (ls < 0) {
            v = ushrRoundHalfUp(v, -ls);
        } else {
            if (ls >= 128) {
                throw new IllegalArgumentException();//WTF? too big
            }
            v = v.leftShift(ls);
        }
        return valueOf(neg, e, v.and(VALUE_MASK));
    }

    private static int numberOfLeadingZeros(Wide l, Wide h) {
        return h.isZero() ? l.numberOfLeadingZeros() + 128 : h.numberOfLeadingZeros();
    }

    private static Quad normalize(Wide low, Wide high, int exp, boolean neg) {
        int lz = numberOfLeadingZeros(low, high);
        if (lz == 256) {
            return neg ? NEGATIVE_ZERO : POSITIVE_ZERO;
        }
        int e = exp - lz + 31;
        if (e >= EXPONENT_MASK) {
            return neg ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
        }
        if (e < 0) {
            e = 0;
        }
        int ls = exp + 16 - e - (e == 0 ? 1 : 0);
        Wide r = ushlrRoundHalfUp(low, high, ls);
        return valueOf(neg, e, r.and(VALUE_MASK));
    }

    private static Wide ushlrRoundHalfUp(Wide low, Wide high, int dist) {
        if (dist <= 0) {
            return ushrRoundHalfUp(high, -dist);
        }
        if (dist >= 256) {
            return Wide.ZERO;
        }
        if (dist < 128) {
            return low.unsignedRightShift(128 - dist).or(high.leftShift(dist))
                    .add(low.leftShift(dist).isNegative() ? Wide.ONE : Wide.ZERO);
        }
        return low.leftShift(dist - 128);
    }

    private static Wide ushrRoundHalfUp(Wide v, int dist) {
        if (dist < 0) {
            throw new IllegalArgumentException();
        }
        if (dist == 0) {
            return v;
        }
        if (dist > 128) {
            return Wide.ZERO;
        }
        if (dist == 128) {
            return v.isNegative() ? Wide.ONE : Wide.ZERO;
        }
        Wide tmp = Wide.ONE.leftShift(dist - 1);
        tmp = v.and(tmp);
        boolean p = !tmp.isZero();
        tmp = v.unsignedRightShift(dist);
        return p ? tmp.increment() : tmp;
    }

    public Quad add(Quad v) {
        Quad a = this, b = v;
        if (a.isNaN() || b.isNaN()) {
            return NaN;
        }
        if (a.isZero()) {
            return b;
        }
        if (b.isZero()) {
            return a;
        }
        boolean sa = a.isNegative(), sb = b.isNegative();
        if (sa && !sb) {
            return b.substract(a.negate());
        }
        if (!sa && sb) {
            return a.substract(b.negate());
        }
        int ea = a.getBiasedExponent(), eb = b.getBiasedExponent();
        if (ea == EXPONENT_MASK) {
            return a;
        }
        if (eb == EXPONENT_MASK) {
            return b;
        }
        int distance = ea - eb;
        if (distance > SIGNIFICANT_BITS + 2) {
            return a;
        }
        if (distance < -SIGNIFICANT_BITS - 2) {
            return b;
        }
        Wide av = a.getValue(), bv = b.getValue();
        if (ea != 0) {
            av = Wide.valueOf(av.low, av.high | (1L << 48));
        } else {
            ea = 1;
            distance++;
        }
        if (eb != 0) {
            bv = Wide.valueOf(bv.low, bv.high | (1L << 48));
        } else {
            eb = 1;
            distance--;
        }
        int resultExponent;
        if (distance < 0) {
            av = ushrRoundHalfUp(av, -distance);
            resultExponent = eb;
        } else {
            bv = ushrRoundHalfUp(bv, distance);
            resultExponent = ea;
        }
        Wide r = av.add(bv);
        return normalize(r, resultExponent, sa);
    }

    public Quad substract(Quad v) {
        Quad a = this, b = v;
        if (a.isNaN() || b.isNaN()) {
            return NaN;
        }
        if (a.isZero()) {
            return b.negate();
        }
        if (b.isZero()) {
            return a;
        }
        boolean sa = a.isNegative(), sb = b.isNegative();
        if (sa ^ sb) {
            return a.add(b.negate());
        }
        int ea = a.getBiasedExponent(), eb = b.getBiasedExponent();
        if (ea == EXPONENT_MASK && eb == EXPONENT_MASK) {
            return NaN;
        }
        if (ea == EXPONENT_MASK) {
            return a;
        }
        if (eb == EXPONENT_MASK) {
            return b.negate();
        }
        int distance = ea - eb;
        if (distance > SIGNIFICANT_BITS + 2) {
            return a;
        }
        if (distance < -SIGNIFICANT_BITS - 2) {
            return b.negate();
        }
        Wide av = a.getValue(), bv = b.getValue();
        if (ea != 0) {
            av = Wide.valueOf(av.low, av.high | (1L << 48));
        } else {
            ea = 1;
            distance++;
        }
        if (eb != 0) {
            bv = Wide.valueOf(bv.low, bv.high | (1L << 48));
        } else {
            eb = 1;
            distance--;
        }
        int resultExponent;
        boolean invert;
        if (less(a, b) ^ sa) {
            invert = true;
            av = ushrRoundHalfUp(av, -distance);
            resultExponent = eb;
        } else {
            invert = false;
            bv = ushrRoundHalfUp(bv, distance);
            resultExponent = ea;
        }
        Wide r = invert ? bv.substract(av) : av.substract(bv);
        return normalize(r, resultExponent, sa ^ invert);
    }

    public Quad multiply(Quad v) {
        Quad a = this, b = v;
        if (a.isNaN() || b.isNaN()) {
            return NaN;
        }
        boolean az = a.isZero(), bz = b.isZero();
        boolean ai = a.isInfinite(), bi = b.isInfinite();
        if ((az && bi) || (ai && bz)) {
            return NaN;
        }
        boolean sa = a.isNegative(), sb = b.isNegative();
        boolean rs = sa ^ sb;
        if (ai || bi) {
            return rs ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
        }
        if (az || bz) {
            return rs ? NEGATIVE_ZERO : POSITIVE_ZERO;
        }
        int ea = a.getBiasedExponent(), eb = b.getBiasedExponent();
        Wide av = a.getValue(), bv = b.getValue();
        if (ea != 0) {
            av = Wide.valueOf(av.low, av.high | (1L << 48));
        } else {
            ea = 1;
        }
        if (eb != 0) {
            bv = Wide.valueOf(bv.low, bv.high | (1L << 48));
        } else {
            eb = 1;
        }
        int resultExponent = ea + eb - EXPONENT_BIAS;
        if (resultExponent >= EXPONENT_MASK) {
            return rs ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
        }
        Wide[] r = av.ufullmultiply(bv);
        return normalize(r[0], r[1], resultExponent, rs);
    }

    private static Wide divideQ(Wide x, Wide y, int[] e) {
        if (y.isZero()) {
            // division by zero
            throw new ArithmeticException();
        }
        int divisorSize = y.numberOfLeadingZeros();
        int dividentSize = x.numberOfLeadingZeros();
        if (divisorSize < 15 || dividentSize < 15) {
            throw new IllegalArgumentException();//too big
        }
        Wide out = ZERO;
        int i = divisorSize - dividentSize;
        e[0] += i;
        if (i > 0) {
            y = y.leftShift(i);
        } else {
            x = x.leftShift(-i);
        }
        if (x.less(y)) {
            x.leftShift(1);
            e[0]++;
        }
        for (i = 0; i < 114; i++) {
            out = out.leftShift(1);
            Wide tmp = x.substract(y);
            if (!tmp.isNegative()) {
                x = tmp;
                out = Wide.valueOf(out.low | 1L, out.high);
            }
            x = x.leftShift(1);
        }
        e[0]--;//because 114 bits
        return out;
    }

    public Quad divide(Quad v) {
        Quad a = this, b = v;
        if (a.isNaN() || b.isNaN()) {
            return NaN;
        }
        boolean az = a.isZero(), bz = b.isZero();
        boolean sa = a.isNegative(), sb = b.isNegative();
        boolean rs = sa ^ sb;
        if (bz) {
            if (az) {
                return NaN;
            }
            return rs ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
        }
        boolean ai = a.isInfinite(), bi = b.isInfinite();
        if (ai) {
            if (bi) {
                return NaN;
            }
            return rs ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
        }
        if (bi) {
            return rs ? NEGATIVE_ZERO : POSITIVE_ZERO;
        }
        int ea = a.getBiasedExponent(), eb = b.getBiasedExponent();
        Wide av = a.getValue(), bv = b.getValue();
        if (ea != 0) {
            av = Wide.valueOf(av.low, av.high | (1L << 48));
        } else {
            ea = 1;
        }
        if (eb != 0) {
            bv = Wide.valueOf(bv.low, bv.high | (1L << 48));
        } else {
            eb = 1;
        }
        int resultExponent = ea - eb + EXPONENT_BIAS;
        if (resultExponent >= EXPONENT_MASK) {
            return rs ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
        }
        int[] e = {resultExponent};
        Wide r = divideQ(av, bv, e);
        return normalize(r, e[0], rs);
    }

    public static Quad valueOf(long l, long h) {
        return new Quad(l, h);
    }

    public static Quad valueOf(boolean neg, int e, Wide v) {
        if (v.high != (v.high & 0xffffffffffffL)) {
            throw new IllegalArgumentException();
        }
        if (e != (e & EXPONENT_MASK)) {
            throw new IllegalArgumentException();
        }
        return new Quad(v.low, v.high | ((long) e << 48) | ((neg ? 1L : 0L) << 63));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Quad)) {
            return false;
        }
        Quad other = (Quad) obj;
        if (this.isNaN() && other.isNaN()) {
            return true;
        }
        return (low == other.low)
                && (high == other.high);
    }

    @Override
    public boolean eq(Quad other) {
        return eq(this, other);
    }

    @Override
    public boolean more(Quad other) {
        return more(this, other);
    }

    @Override
    public boolean less(Quad other) {
        return less(this, other);
    }

    public static boolean neq(Quad a, Quad b) {
        return a.neq(b);
    }

    public static boolean moreOrEq(Quad a, Quad b) {
        return a.moreOrEq(b);
    }

    public static boolean lessOrEq(Quad a, Quad b) {
        return a.lessOrEq(b);
    }

    public static boolean eq(Quad a, Quad b) {
        if (a.isNaN() || b.isNaN()) {
            return false;
        }
        if (a.isZero() && b.isZero()) {
            return true;
        }
        return (a.low == b.low) && (a.high == b.high);
    }

    public static boolean more(Quad a, Quad b) {
        if (a.isNaN() || b.isNaN()) {
            return false;
        }
        boolean az = a.isZero(), bz = b.isZero();
        if (az && bz) {
            return false;
        }
        boolean sa = a.isNegative(), sb = b.isNegative();
        if (az) {
            return sb;
        }
        if (bz) {
            return !sa;
        }
        if (sa ^ sb) {
            return !sa;
        }
        int ea = a.getBiasedExponent(), eb = b.getBiasedExponent();
        if (ea > eb) {
            return !sa;
        }
        if (eb > ea) {
            return sa;
        }
        Wide av = a.getValue(), bv = b.getValue();
        if (ea != 0) {
            av = Wide.valueOf(av.low, av.high | (1L << 48));
        }
        if (eb != 0) {
            bv = Wide.valueOf(bv.low, bv.high | (1L << 48));
        }
        return sa ^ Wide.more(av, bv);
    }

    public static boolean less(Quad a, Quad b) {
        if (a.isNaN() || b.isNaN()) {
            return false;
        }
        boolean az = a.isZero(), bz = b.isZero();
        if (az && bz) {
            return false;
        }
        boolean sa = a.isNegative(), sb = b.isNegative();
        if (az) {
            return !sb;
        }
        if (bz) {
            return sa;
        }
        if (sa ^ sb) {
            return sa;
        }
        int ea = a.getBiasedExponent(), eb = b.getBiasedExponent();
        if (ea > eb) {
            return sa;
        }
        if (eb > ea) {
            return !sa;
        }
        Wide av = a.getValue(), bv = b.getValue();
        if (ea != 0) {
            av = Wide.valueOf(av.low, av.high | (1L << 48));
        }
        if (eb != 0) {
            bv = Wide.valueOf(bv.low, bv.high | (1L << 48));
        }
        return sa ^ Wide.less(av, bv);
    }

    @Override
    public int compareTo(Quad obj) {
        return compare(this, obj);
    }

    public static int compare(Quad a, Quad b) {
        boolean sa = a.isNegative(), sb = b.isNegative();
        int r = sa ? -1 : 1;
        if (sa ^ sb) {
            return r;
        }
        int ea = a.getBiasedExponent(), eb = b.getBiasedExponent();
        if (ea > eb) {
            return r;
        }
        if (eb > ea) {
            return -r;
        }
        return r * Wide.compare(a.getValue(), b.getValue());
    }

    @Override
    public int hashCode() {
        return (int) (low ^ high ^ (low >>> 32) ^ (high >>> 32));
    }

    @Override
    public String toString() {
        return "Quad[s=" + getSign() + ", e=" + getBiasedExponent() + ", v=" + getValue().toHexString() + "]";
    }
}
