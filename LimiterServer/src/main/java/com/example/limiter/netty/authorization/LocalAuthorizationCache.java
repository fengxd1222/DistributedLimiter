package com.example.limiter.netty.authorization;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author feng xud
 * Translation: Temporarily use a local  `Map`  to cache the token. Upgrade to Redis in the future.
 */
@Component
public class LocalAuthorizationCache implements AuthorizationCache {

    private static final ConcurrentHashMap<String,String> cache = new ConcurrentHashMap<>();

    @Override
    public String checkout(String token) {
        return cache.get(token);
    }

    @Override
    public void putToken(String token, String value,long expireTime) {
        cache.put(token,value);
    }
}
