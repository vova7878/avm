package com.v7878.avm.utils;

public interface Comparable2<T> {

    public boolean eq(T other);

    public default boolean neq(T other) {
        return !eq(other);
    }

    public boolean more(T other);

    public default boolean moreOrEq(T other) {
        return eq(other) || more(other);
    }

    public boolean less(T other);

    public default boolean lessOrEq(T other) {
        return eq(other) || less(other);
    }
}
