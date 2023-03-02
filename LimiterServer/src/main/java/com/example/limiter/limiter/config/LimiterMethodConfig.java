package com.example.limiter.limiter.config;

import com.example.limiter.limiter.DistributedQPSLimiter;
import com.example.limiter.netty.remote.LimiterDefinition;

import java.util.concurrent.ConcurrentHashMap;

public class LimiterMethodConfig {


    private final ConcurrentHashMap<String, DistributedQPSLimiter> DEFINITION_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();



    public void put(String methodKey,LimiterDefinition definition){
        synchronized (this){
            DEFINITION_CONCURRENT_HASH_MAP.compute(methodKey,(k,v)->{
                return new DistributedQPSLimiter(definition);
            });
        }
    }

    public ConcurrentHashMap getAllLimiterMap(){
        return DEFINITION_CONCURRENT_HASH_MAP;
    }

    public DistributedQPSLimiter getLimiter(String methodKey){
        return DEFINITION_CONCURRENT_HASH_MAP.get(methodKey);
    }

}
