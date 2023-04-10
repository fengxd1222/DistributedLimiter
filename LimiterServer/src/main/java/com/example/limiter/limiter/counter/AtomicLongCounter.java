package com.example.limiter.limiter.counter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author feng xud
 */
public class AtomicLongCounter implements Counter {
    private final AtomicLong slot = new AtomicLong();


    @Override
    public void increaseSlot() {
        slot.incrementAndGet();
    }

    @Override
    public boolean compareAndInc(long oldValue, long updateValue) {
        return slot.compareAndSet(oldValue,updateValue);
    }

    @Override
    public long getCount() {
        return slot.get();
    }
}
