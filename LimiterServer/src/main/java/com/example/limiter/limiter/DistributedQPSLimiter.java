package com.example.limiter.limiter;

import com.example.limiter.netty.remote.LimiterDefinition;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  * 分布式限流  基于单机实现集群式
 *  * 将单机限流作为一个实例服务，需要实现资源节点-次数的映射
 *  * <p>
 *  * 同时需要自实现RPC-netty
 */
public class DistributedQPSLimiter extends QPSLimiter{

    private LimiterDefinition limiterDefinition;



    public DistributedQPSLimiter(LimiterDefinition definition) {

        super(definition.getQps(),definition.getLimit(),definition.getTime(),1800);
        this.limiterDefinition = definition;
    }
}
