package com.example.limiter.limiter.strategy;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChannelReadHolder implements ApplicationContextAware {
    public static final String LIMITER_CONFIG_HANDLER = "LimiterConfig";
    public static final String LIMITER_HANDLER = "LimiterInfo";


    static Map<String, ChannelReadHandlerStrategy> channelReadHandlerStrategyMap;

    public static <T> T handle(Object object,String clientId) {
        return (T) channelReadHandlerStrategyMap.get(object.getClass().getSimpleName()).doReadHandle(object,clientId);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        channelReadHandlerStrategyMap = applicationContext.getBeansOfType(ChannelReadHandlerStrategy.class);
    }


//    public enum ChannelReadHandlerEnum{
//
//        LIMITER_CONFIG_HANDLER(),
//        LIMITER_HANDLER(),
//
//        ;
//
//        private String handlerName;
//
//        ChannelReadHandlerEnum(String handlerName) {
//            this.handlerName = handlerName;
//        }
//
//        public String getHandlerName() {
//            return handlerName;
//        }
//    }
}
