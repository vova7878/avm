package com.v7878.avm.utils;

public interface Tree<T> {

    boolean isFull();

    int put(T value);

    //TODO: T put(int position, T value);
    T get(int position);

    T remove(int position);
}
