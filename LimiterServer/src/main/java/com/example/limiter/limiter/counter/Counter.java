package com.example.limiter.limiter.counter;

/**
 * @author feng xud
 */
public interface Counter {
    public void increaseSlot();

    public boolean compareAndInc(long oldValue, long updateValue);

    public long getCount();
}
