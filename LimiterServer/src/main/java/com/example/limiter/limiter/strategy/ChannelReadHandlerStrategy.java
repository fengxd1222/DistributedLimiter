package com.example.limiter.limiter.strategy;

public interface ChannelReadHandlerStrategy<M> {

    public <T>T doReadHandle(M m,String clientId);
}
