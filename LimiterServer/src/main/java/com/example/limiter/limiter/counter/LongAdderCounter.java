package com.example.limiter.limiter.counter;


import com.example.limiter.limiter.counter.Counter;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author feng xud
 */
public class LongAdderCounter implements Counter {

    private final LongAdder slot = new LongAdder();


    @Override
    public void increaseSlot() {
        slot.increment();
    }

    @Override
    public boolean compareAndInc(long oldValue, long updateValue) {
        slot.add(updateValue-oldValue);
        return true;
    }

    @Override
    public long getCount() {
        return slot.sum();
    }
}
