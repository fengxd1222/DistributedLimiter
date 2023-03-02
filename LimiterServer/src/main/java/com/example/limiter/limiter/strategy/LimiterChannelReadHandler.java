package com.example.limiter.limiter.strategy;

import com.example.limiter.limiter.DistributedQPSLimiter;
import com.example.limiter.limiter.LimiterInfo;
import com.example.limiter.limiter.config.LimiterMethodConfig;
import com.example.limiter.limiter.config.LimiterRemoteConfig;
import org.springframework.stereotype.Component;

@Component(ChannelReadHolder.LIMITER_HANDLER)
public class LimiterChannelReadHandler implements ChannelReadHandlerStrategy<LimiterInfo> {

    @Override
    public Boolean doReadHandle(LimiterInfo limiterInfo, String clientId) {
        LimiterMethodConfig config = LimiterRemoteConfig.getConfig(clientId);
        if(config==null){
            throw new RuntimeException("config is null");
        }

        DistributedQPSLimiter limiter = config.getLimiter(limiterInfo.getMethodKey());
        if(limiter==null){
            throw new RuntimeException("limiter is null");
        }
        return limiter.tryInc(limiterInfo.getCurTime());
    }
}
