package com.example.limiter.limiter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流器 抽象类
 */
public abstract class AbstractLimiter {

    volatile int qps;

    //分多少段
    volatile int limit = 5;

    //窗口范围 秒
    volatile long timeDuration = 5;

    //超时时间 此时间需要比timeDuration大 单位 秒
    volatile long keepaliveTime;
    //查询中
    static final Integer PROCESSING = 1;
    //空闲
    static final Integer IDLE = 0;
    //重建中
    static final Integer REBUILDING = -1;
    //重建标识
    final AtomicInteger rebuildState = new AtomicInteger(IDLE);
    /**
     * 尝试访问
     *  true 放行
     *  false 拒绝
     * @return
     */
    public abstract boolean tryInc(long curTime);

    /**
     * 窗口右滑 也就是动态扩容右边界，缩小左边界
     * @param curTime
     */
    protected abstract void rebuildAndMoveSlotWindow(long curTime);

    /**
     * 初始化窗口
     * @param limit
     * @param timeDuration
     */
    protected abstract void initSlotWindow(int limit, long timeDuration);

    /**
     * 寻找窗口并判断值
     * @param cur 时间毫秒
     * @return true 放行  false  拒绝
     */
    protected abstract boolean findThenCheckSlot(long cur);


    /**
     * 加锁方式 子类实现
     * @return
     */
    public boolean lock(){
        throw new UnsupportedOperationException();
    }

    /**
     * 解锁方式 子类实现
     * @return
     */
    public boolean unLock(){
        throw new UnsupportedOperationException();
    }

    /**
     * 加锁方式 子类实现 提供给cas方式的加锁
     * @param oldValue
     * @param update
     * @return
     */
    public boolean lock(int oldValue,int update){
        throw new UnsupportedOperationException();
    }

    /**
     * 解锁方式 子类实现 提供给cas方式的解锁
     * @param update
     * @return
     */
    public boolean unLock(int update){
        throw new UnsupportedOperationException();
    }
}
