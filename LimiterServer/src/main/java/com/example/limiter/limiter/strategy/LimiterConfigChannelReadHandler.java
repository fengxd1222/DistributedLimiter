package com.example.limiter.limiter.strategy;

import com.example.limiter.limiter.LimiterConfig;
import com.example.limiter.limiter.config.LimiterMethodConfig;
import com.example.limiter.limiter.config.LimiterRemoteConfig;
import com.example.limiter.netty.remote.LimiterDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;

@Component(value = ChannelReadHolder.LIMITER_CONFIG_HANDLER)
public class LimiterConfigChannelReadHandler implements ChannelReadHandlerStrategy<LimiterConfig> {

    @Override
    public Void doReadHandle(LimiterConfig limiterConfig, String clientId) {
        Assert.notNull(clientId,"The argument of clientId to this method ["+this.getClass()+".doReadHandle] cannot be null ");
        List<LimiterDefinition> var1 = limiterConfig.getLimiterDefinitions();
        if (var1.isEmpty()) return null;
        Iterator<LimiterDefinition> var2 = var1.iterator();
        LimiterMethodConfig limiterMethodConfig = new LimiterMethodConfig();
        while (var2.hasNext()) {
            LimiterDefinition next = var2.next();
            limiterMethodConfig.put(next.getMethodKey(), next);
        }
        //初始化滑动窗口
        LimiterRemoteConfig.put(clientId,limiterMethodConfig);
        return null;
    }
}
