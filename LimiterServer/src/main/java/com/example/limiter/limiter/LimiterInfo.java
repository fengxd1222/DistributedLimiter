package com.example.limiter.limiter;

public class LimiterInfo {
    //method
    private String methodKey;

    //time
    private long curTime = System.currentTimeMillis();

    public LimiterInfo(String methodKey) {
        this.methodKey = methodKey;
    }

    public LimiterInfo() {
    }

    public String getMethodKey() {
        return methodKey;
    }

    public void setMethodKey(String methodKey) {
        this.methodKey = methodKey;
    }

    public long getCurTime() {
        return curTime;
    }

    public void setCurTime(long curTime) {
        this.curTime = curTime;
    }
}
