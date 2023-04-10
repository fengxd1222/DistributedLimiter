package com.example.limiter.limiter.counter;

/**
 * @author feng xud
 */
public class CounterFactory<T> {
    public T newInstance(Class<? extends T> cls){
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
