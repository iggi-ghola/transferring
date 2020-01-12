package com.kozhekin.transferring.datastore;

import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryIdGenerator implements IdGenerator {
    private final AtomicInteger ids;
    private boolean isFull;

    public InMemoryIdGenerator() {
        this.ids = new AtomicInteger(0);
    }

    public int next() {
        if (isFull) {
            return -1;
        }
        int val = ids.getAndIncrement();
        if (val < 0) {
            isFull = true;
            return -1;
        }
        return val;
    }
}
