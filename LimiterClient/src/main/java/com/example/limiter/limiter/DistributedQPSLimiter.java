package com.example.limiter.limiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author feng xud
 * @DistributedQPSLimiter(qps=100,time=5,limit=10)
 * 将5s分为10段，构建滑动窗口，滑动范围是10段，且这10段的qps不能超过100
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedQPSLimiter {
    //qps
    int qps() default 100;
    //时间单位
    long time() default 1L;
    //时间分成limit段
    int limit() default 5;
}
