package com.v7878.avm.utils;

public class Tree16<T> implements Tree<T> {

    private final Object[] data = new Object[16];
    private boolean full;

    @Override
    public boolean isFull() {
        return full;
    }

    public boolean isEmpty() {
        boolean empty = true;
        for (int i = 0; i < 16; i++) {
            if (data[i] != null) {
                empty = false;
                break;
            }
        }
        return empty;
    }

    @Override
    public int put(T value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        return put(value, 7);
    }

    @SuppressWarnings("unchecked")
    private int put(T value, int level) {
        if (isFull()) {
            throw new IllegalStateException();
        }
        int out = 0;
        boolean tmp_full = true;
        boolean set = false;
        if (level == 0) {
            for (int i = 0; i < 16; i++) {
                if (data[i] == null) {
                    if (set) {
                        tmp_full = false;
                        break;
                    } else {
                        data[i] = value;
                        out = i;
                        set = true;
                    }
                }
            }
        } else {
            for (int i = 0; i < 16; i++) {
                Tree16<T> child = (Tree16<T>) data[i];
                if (child == null || !child.full) {
                    if (set) {
                        tmp_full = false;
                        break;
                    } else {
                        if (child == null) {
                            child = new Tree16<>();
                            data[i] = child;
                        }
                        int tmp_position = child.put(value, level - 1);
                        out = (tmp_position << 4) | i;
                        if (!child.full) {
                            tmp_full = false;
                            break;
                        }
                        set = true;
                    }
                }
            }
        }
        full = tmp_full;
        return out;
    }

    /*@Override
    public T put(int position, T value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        return (T) put(position, value, 7);
    }

    private Object put(int position, T value, int level) {
        int num = position & 0b1111;
        if (level == 0) {
            Object last = null;
            boolean set = false;
            for (int i = 0; i < 16; i++) {
                
            }
            return last;
        }
        Tree16 child = (Tree16) data[num];
        if (child == null) {
            child = new Tree16();
            data[num] = child;
        }
        return child.put(position >>> 4, value, level - 1);
    }*/
    @Override
    @SuppressWarnings("unchecked")
    public T get(int position) {
        return (T) get(position, 7);
    }

    private Object get(int position, int level) {
        int num = position & 0b1111;
        if (level == 0) {
            return data[num];
        }
        @SuppressWarnings("unchecked")
        Tree16<T> child = (Tree16<T>) data[num];
        return child == null ? null : child.get(position >>> 4, level - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T remove(int position) {
        return (T) remove(position, 7);
    }

    private Object remove(int position, int level) {
        int num = position & 0b1111;
        Object obj;
        if (level == 0) {
            obj = data[num];
            data[num] = null;
        } else {
            @SuppressWarnings("unchecked")
            Tree16<T> child = (Tree16<T>) data[num];
            if (child == null) {
                obj = null;
            } else {
                obj = child.remove(position >>> 4, level - 1);
                if (child.isEmpty()) {
                    data[num] = null;
                }
            }
        }
        if (obj != null && full) {
            full = false;
        }
        return obj;
    }
}
