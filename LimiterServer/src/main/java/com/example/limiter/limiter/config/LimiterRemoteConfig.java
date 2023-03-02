package com.example.limiter.limiter.config;

import java.util.concurrent.ConcurrentHashMap;

public class LimiterRemoteConfig {
    private static final ConcurrentHashMap<String, LimiterMethodConfig> REMOTE_ADDRESS_AND_METHOD_MAP = new ConcurrentHashMap<>();


    public static void put(String remoteAddress, LimiterMethodConfig limiterMethodConfig) {
        REMOTE_ADDRESS_AND_METHOD_MAP.put(remoteAddress, limiterMethodConfig);
    }

    public static ConcurrentHashMap getAllConfigMap() {
        return REMOTE_ADDRESS_AND_METHOD_MAP;
    }

    public static LimiterMethodConfig getConfig(String remoteAddress) {
        return REMOTE_ADDRESS_AND_METHOD_MAP.get(remoteAddress);
    }

}
