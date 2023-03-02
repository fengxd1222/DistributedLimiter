package com.example.limiter.netty.remote;

public class LimiterDefinition {
    private int qps;

    private int limit;

    private long time;

    private String methodKey;

    public LimiterDefinition(int qps, int limit, long time, String methodKey) {
        this.qps = qps;
        this.limit = limit;
        this.time = time;
        this.methodKey = methodKey;
    }

    public LimiterDefinition() {
    }

    public int getQps() {
        return qps;
    }

    public void setQps(int qps) {
        this.qps = qps;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMethodKey() {
        return methodKey;
    }

    public void setMethodKey(String methodKey) {
        this.methodKey = methodKey;
    }

    @Override
    public String toString() {
        return "LimiterDefinition{" +
                "qps=" + qps +
                ", limit=" + limit +
                ", time=" + time +
                ", methodKey='" + methodKey + '\'' +
                '}';
    }
}
