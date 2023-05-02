package com.example.limiter.netty.authorization;

/**
 * @author feng xud
 */
public interface AuthorizationCache {

    public String checkout(String token);

    public void putToken(String token,String value,long expireTime);

}
