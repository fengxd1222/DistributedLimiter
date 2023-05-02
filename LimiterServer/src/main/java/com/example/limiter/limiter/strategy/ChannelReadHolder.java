package com.example.limiter.limiter.strategy;

import com.example.limiter.netty.authorization.AuthorizationCache;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author feng xud
 */
@Component
public class ChannelReadHolder implements ApplicationContextAware {
    public static final String LIMITER_CONFIG_HANDLER = "LimiterConfig";
    public static final String LIMITER_HANDLER = "LimiterInfo";


    static Map<String, ChannelReadHandlerStrategy> channelReadHandlerStrategyMap;
    static AuthorizationCache tokenCacheHandler;

    @SuppressWarnings("unchecked")
    public static <T> T handle(Object object,String clientId) {
        return (T) channelReadHandlerStrategyMap.get(object.getClass().getSimpleName()).doReadHandle(object,clientId);
    }

    public static AuthorizationCache getTokenCacheHandler(){
        return tokenCacheHandler;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        channelReadHandlerStrategyMap = applicationContext.getBeansOfType(ChannelReadHandlerStrategy.class);
        tokenCacheHandler = applicationContext.getBean(AuthorizationCache.class);
    }
}
