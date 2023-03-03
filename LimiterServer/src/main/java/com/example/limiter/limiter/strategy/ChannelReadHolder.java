package com.example.limiter.limiter.strategy;

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

    @SuppressWarnings("unchecked")
    public static <T> T handle(Object object,String clientId) {
        return (T) channelReadHandlerStrategyMap.get(object.getClass().getSimpleName()).doReadHandle(object,clientId);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        channelReadHandlerStrategyMap = applicationContext.getBeansOfType(ChannelReadHandlerStrategy.class);
    }
}
