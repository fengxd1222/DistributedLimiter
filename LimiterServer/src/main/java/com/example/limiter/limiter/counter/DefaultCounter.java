package com.example.limiter.limiter.counter;

/**
 * @author feng xud
 */
public class DefaultCounter implements Counter{
    @Override
    public void increaseSlot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compareAndInc(long oldValue, long updateValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getCount() {
        throw new UnsupportedOperationException();
    }
}
