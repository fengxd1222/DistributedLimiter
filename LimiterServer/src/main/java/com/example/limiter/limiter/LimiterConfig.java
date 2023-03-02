package com.example.limiter.limiter;

import com.example.limiter.netty.remote.LimiterDefinition;

import java.util.List;

public class LimiterConfig {
    private List<LimiterDefinition> limiterDefinitions;

    public LimiterConfig(List<LimiterDefinition> limiterDefinitions) {
        this.limiterDefinitions = limiterDefinitions;
    }

    public LimiterConfig() {
    }

    public List<LimiterDefinition> getLimiterDefinitions() {
        return limiterDefinitions;
    }

    public void setLimiterDefinitions(List<LimiterDefinition> limiterDefinitions) {
        this.limiterDefinitions = limiterDefinitions;
    }
}
