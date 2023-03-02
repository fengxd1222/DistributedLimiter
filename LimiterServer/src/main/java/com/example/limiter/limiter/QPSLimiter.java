package com.example.limiter.limiter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 写一个基于qps的单机滑动时间窗口算法
 * <p>
 * 将一分钟分为6个部分，每个部分10秒钟
 */
public class QPSLimiter extends AbstractLimiter {

//    private static final Logger log = org.slf4j.LoggerFactory.getLogger(QPSLimiter.class);


    //窗口数组数量
    private int slotWindowLength;


    //窗口数组
    private SlotWindow[] slotWindows;

    //窗口map，负责通过时间查找
    private final TreeMap<Long, SlotWindow> slotWindowCache = new TreeMap<>();

    //窗口阈值，定位点在其之后会触发
    private final int thresholdIndex;

    //头节点 虚拟节点
    private SlotWindow head = new SlotWindow(true);
    //尾节点 虚拟节点
    private SlotWindow tail = new SlotWindow(false);

    //后台线程，负责超时回收内存
    private final Worker worker;
    //锁
    private final ReentrantLock lock = new ReentrantLock();

    QPSLimiter(int qps, int limit, long timeDuration, long keepaliveTime) {
        this.qps = qps;
        this.limit = limit;
        this.timeDuration = timeDuration;
        this.keepaliveTime = keepaliveTime;
        //初始化窗口数组，保证窗口的滑动，数组默认为limit的2倍，减少rebuild的次数
        this.slotWindowLength = limit << 1;
        slotWindows = new SlotWindow[slotWindowLength];
        //默认触发rebuild的阈值为0.75，即访问的时间在数组的后1/4处，触发rebuild
        this.thresholdIndex = (int) (slotWindows.length * 0.75);
        //初始化窗口数组及缓存
        initSlotWindow(limit, timeDuration);
        this.worker = tryWork();
    }

    public QPSLimiter(int qps, int limit, long timeDuration, int slotWindowLength, long keepaliveTime) {
        this.qps = qps;
        this.limit = limit;
        this.timeDuration = timeDuration;
        this.keepaliveTime = keepaliveTime;
        //初始化窗口数组，保证窗口的滑动，数组长度由用户指定
        this.slotWindowLength = slotWindowLength;
        slotWindows = new SlotWindow[slotWindowLength];
        //默认触发rebuild的阈值为0.75，即访问的时间在数组的后1/4处，触发rebuild
        this.thresholdIndex = (int) (slotWindows.length * 0.75);
        //初始化窗口数组及缓存
        initSlotWindow(limit, timeDuration);
        this.worker = tryWork();
    }

    /**
     * 初始化滑动窗口数组以及缓存
     * 如 limit=10，timeDuration=1 构建出数量为10的窗口，每个窗口为100ms（timeDuration/limit => 1s/10=100ms）
     *
     * @param limit        滑动窗口的数量
     * @param timeDuration 滑动窗口对应的时间长度 单位：秒
     */
    @Override
    protected final void initSlotWindow(int limit, long timeDuration) {
        lock.lock();
        try {
            long cur = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() * 1000;
            SlotWindow head = this.head;
            long l = 1000 * timeDuration / limit;
            slotWindows = new SlotWindow[slotWindowLength];
            for (int i = 0; i < slotWindows.length; i++) {
                SlotWindow slotWindow = slotWindows[i] = new SlotWindow(cur);
                slotWindowCache.put(slotWindow.time, slotWindow);
                cur = cur + l;
                head.next = slotWindow;
                slotWindow.pre = head;
                head = slotWindow;
            }
            head.next = this.tail;
            this.tail.pre = head;
        } finally {
            lock.unlock();
        }
    }


    /**
     * 尝试增加一次访问次数，获取不到cas状态会自旋等待，正常情况自旋1-2次
     *
     * @return
     */
    public boolean tryInc(long curTime) {
        long cur = curTime==0?System.currentTimeMillis():curTime;
        boolean checkOut = false;
        try {
            //自旋，一般循环1-2次
            for (; ; ) {
                //第一次判断 过滤掉可能引发slotWindows[thresholdIndex]异常的调用
                if (rebuildState.get() >= 0) {
                    //判断当前时间是否在数组的后1/4处
                    if (slotWindows.length > 0 && slotWindows[thresholdIndex].time < cur) {
                        //第二次判断 只有在空闲状态中才可重建数据，否则自旋等待
                        //自旋一次获取不到cas，就尝试直接去查询，但是查询时依旧要走查询的条件判断，查询到了此次任务结束
                        if (lock(IDLE, REBUILDING)) {
                            //true 重建并移动其中可用的部分链表
//                            log.info(Thread.currentThread().getName()+" 重建链表 "+ this.rebuildState);
                            rebuildAndMoveSlotWindow(cur);
                            checkOut = true;
                        }
                    }//判断时间是否已超过现有窗口的值 或者窗口已经被回收
                    else if (head.next == null || cur > tail.pre.time) {
                        //如果已经超过，需要重新生成窗口
//                        log.info("需要重新生成链表: " + this);
                        if (lock(IDLE, REBUILDING)) {
                            initSlotWindow(limit, timeDuration);
                            checkOut = true;
                        }
//                        log.info("重新生成链表后: " + this);
                    }
                }
                //判断是否已经由当前的线程重建过，或者当前状态是否空闲或者查询中，这两种状态可共享，不涉及内存修改，不会出现线程安全问题
                if (checkOut || lock(IDLE, PROCESSING) || lock(PROCESSING, PROCESSING)) {
                    return findThenCheckSlot(cur);
                }
            }
        } finally {
            //最终修改空闲
            unLock(IDLE);
        }
    }

//    /**
//     * 尝试增加一次访问次数
//     * @return
//     */
//    public boolean tryInc() {
//        lock.lock();
//        try {
//            long cur = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() * 1000;
//            //判断当前时间是否在数组的后1/4处
//            if (slotWindows[thresholdIndex].time < cur) {
//
//                    //true 重建并移动其中可用的部分链表
//                    System.out.println("重建并移动部分链表: " + this);
//                    rebuildAndMoveSlotWindow(cur);
//                    System.out.println("重建后链表: " + this);
//            }//判断时间是否已超过现有窗口的值
//            else if ((cur + timeDuration) > tail.pre.time || cur < head.next.time) {
//                //如果已经超过，需要重新生成窗口
//                System.out.println("需要重新生成链表: " + this);
//
//                    initSlotWindow(limit, timeDuration);
//
//            }
//            //真正的添加和判断逻辑
//            return findThenCheckSlot(cur);
//        } finally {
//            lock.unlock();
//        }
//    }

    /**
     * 寻找当前窗口，并判断qps是否已经超过阈值，超过返回false，没有超过，使用cas进行+1操作
     *
     * @param cur
     * @return
     */
    @Override
    protected final boolean findThenCheckSlot(long cur) {
        SlotWindow slotWindow = getSlotWindow(cur);
        SlotWindow nSlot = slotWindow;
        int limit = this.limit;
        int count = 0;
        for (; ; ) {
            int oldCount = nSlot.getCount();
            while (limit != 0 && slotWindow != head && count < qps) {
                count += slotWindow.getCount();
                slotWindow = slotWindow.pre;
                limit--;
            }
            if (count >= qps) {
                return false;
            }
            if (count < qps && nSlot.compareAndInc(oldCount, oldCount + 1)) {
                return true;
            }
        }
    }

    private SlotWindow getSlotWindow(long cur) {
        Map.Entry<Long, SlotWindow> entry = slotWindowCache.floorEntry(cur);
        if (entry == null) {
            initSlotWindow(limit, timeDuration);
            entry = slotWindowCache.floorEntry(cur);
        }
        return entry.getValue();
    }

    private Long getSlotWindowKey(long cur) {
        return slotWindowCache.floorKey(cur);
    }


    /**
     * 重建，并移动旧数组中可用的窗口，以cur时间为节点，向前置节点寻找limit为准
     *
     * @param cur
     */
    @Override
    protected final void rebuildAndMoveSlotWindow(long cur) {
        //获取当前时间对应的窗口的左边界进行重新构建
        SlotWindow slotWindow = slotWindows[calPreIndex(cur)];
        slotWindows = new SlotWindow[slotWindowLength];
        slotWindowCache.clear();
        //清空slotWindow的前置节点
        // 将边界之前的节点指向空，都是边界节点的前置节点指向空，这个时候链表相当于中断
        slotWindow.pre.next = null;
        //重新指定头节点的next 将head头结点与边界重新相连，废弃的节点gc回收
        SlotWindow head = this.head;
        head.next = slotWindow;
        slotWindow.pre = head;
        long l = 1000 * timeDuration / limit;
        for (int i = 0; i < slotWindows.length; i++) {
            if (slotWindow != tail) {
                //原数据通过链表访问，迁移回新数组
                slotWindows[i] = slotWindow;
                slotWindowCache.put(slotWindow.time, slotWindow);
                head = slotWindow;
                slotWindow = slotWindow.next;
            } else {
                //余下位置，用新窗口补足
                SlotWindow newSlot = new SlotWindow(head.time + l);
                slotWindowCache.put(newSlot.time, newSlot);
                head.next = newSlot;
                newSlot.pre = head;
                head = newSlot;
                slotWindows[i] = newSlot;
            }
        }
        head.next = this.tail;
        this.tail.pre = head;
    }

    /**
     * 通过当前时间，计算出当前滑动窗口的左边界
     *
     * @param cur
     * @return
     */
    private int calPreIndex(long cur) {
        Long slotWindowKey = getSlotWindowKey(cur);
        int index = (int) ((slotWindowKey - slotWindows[0].time) / (1000 * timeDuration / limit));
        return Math.max(index - limit, 0);
    }

    @Override
    public String toString() {
        return "QPSLimiter{" + "slotWindows=" + Arrays.toString(slotWindows) + ", thresholdIndex=" + thresholdIndex + '}';
    }

    private static final class SlotWindow {

        //窗口数组
        private final AtomicInteger slot;

        private final long time;

        private SlotWindow pre;

        private SlotWindow next;


        SlotWindow(long time) {
            slot = new AtomicInteger(0);
            this.time = time;
        }

        SlotWindow(boolean headOrTail) {
            slot = new AtomicInteger(headOrTail ? 0 : -1);
            this.time = headOrTail ? 0 : -1;
        }

        public void increaseSlot() {
            slot.incrementAndGet();
        }

        public boolean compareAndInc(int oldValue, int updateValue) {
            return slot.compareAndSet(oldValue, updateValue);
        }

        public int getCount() {
            return slot.get();
        }

        @Override
        public String toString() {
            return "SlotWindow{" + "slot=" + slot + ", time=" + time + '}';
        }
    }

    /**
     * 限流器内部维护一个线程，线程用于在timeDuration后，回收空间
     * 当timeDuration后不使用，下次访问也会重新创建，那么不如直接将这部分内存释放
     */
    private final class Worker implements Runnable {
        final Thread thread;

        Runnable task;

        Worker(Runnable task) {
            this.task = task;
            this.thread = new Thread(this);
        }

        @Override
        public void run() {
            releaseCache(this);
        }
    }

    private void releaseCache(Worker worker) {

        long time = 1000000000L * keepaliveTime;
        for (; ; ) {
            LockSupport.parkNanos(time);
            SlotWindow lastWindow = tail.pre;
//            log.info("==="+lastWindow);
            if (lastWindow == null || lastWindow.time + keepaliveTime >= System.currentTimeMillis()) {
                continue;
            }
            Runnable task = worker.task;
            try {
                if (lock.tryLock()) {
                    task.run();
                }
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    private Worker tryWork() {
        Worker worker1 = new Worker(() -> {
            if (lock(IDLE, REBUILDING)) {
//                log.info("限流器回收内存 ... "+this);
                slotWindowCache.clear();
                slotWindows = new SlotWindow[0];
                SlotWindow h_next = this.head.next;
                this.head.next = null;
                h_next.pre = null;
                SlotWindow t_pre = this.tail.pre;
                this.tail.pre = null;
                t_pre.next = null;
                unLock(IDLE);
//                log.info("限流器回收内存完毕 ... "+this);
            }
        });
        final Thread thread = worker1.thread;
        thread.start();
        return worker1;
    }

    @Override
    public boolean lock(int oldValue, int update) {
        return rebuildState.compareAndSet(oldValue, update);
    }

    @Override
    public boolean unLock(int update) {
        rebuildState.set(update);
        return true;
    }
}
