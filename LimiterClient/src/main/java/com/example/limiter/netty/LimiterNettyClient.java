package com.example.limiter.netty;

import com.example.limiter.limiter.LimiterScanner;
import com.example.limiter.netty.handler.ClientLimiterEncoderAbstract;
import com.example.limiter.netty.handler.ClientLimiterRequestExecuteHandler;
import com.example.limiter.netty.handler.KryoSerializerDecoder;
import com.example.limiter.netty.remote.LimiterDefinition;
import com.example.limiter.netty.util.EventLoopGroupBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author feng xud
 */
@Component
@Order(1)
public class LimiterNettyClient extends Client implements CommandLineRunner {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LimiterNettyClient.class);

    private Bootstrap bootstrap;

    private EventLoopGroup childGroup;

    /**
     * 锁
     */
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * 用来进行 客户端线程与重连线程的通信
     */
    private final Condition condition = lock.newCondition();
    /**
     * 限流注解扫描器
     */
    private static final LimiterScanner SCANNER = new LimiterScanner();

    public LimiterNettyClient(ClientLimiterRequestExecuteHandler requestExecuteHandler) {
        super(requestExecuteHandler);
    }


    public void run() {
        List<LimiterDefinition> limiterDefinitions = SCANNER.doScan();
        //针对不同系统，启用不同的socket channel
        EventLoopGroupBuilder.Group<MultithreadEventLoopGroup> group = EventLoopGroupBuilder.build();
        //用来接收响应的处理器，内置FutureTask监听
        requestExecuteHandler.setClientId(clientId);
        requestExecuteHandler.setLimiterDefinitions(limiterDefinitions);
        try {
            //初始化
            initBootStrap(group, requestExecuteHandler);
            EXECUTOR.execute(new Worker(requestExecuteHandler));
            connect(requestExecuteHandler);
        } catch (Exception e) {
            log.error("client error ", e);
        } finally {
            childGroup.shutdownGracefully();
        }
    }

    /**
     * 初始化BootStrap
     *
     * @param group                 MultithreadEventLoopGroup构造器
     * @param requestExecuteHandler 发送request请求后，Response的接收处理器
     */
    private void initBootStrap(EventLoopGroupBuilder.Group<MultithreadEventLoopGroup> group, ClientLimiterRequestExecuteHandler requestExecuteHandler) {
        bootstrap = new Bootstrap();
        childGroup = group.getC();
        bootstrap.group(childGroup)
                .channel(group.getChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new NettyClientChannelInitializer(requestExecuteHandler));
    }

    /**
     * 初始化连接 首次调用
     *
     * @param requestExecuteHandler 发送request请求后，Response的接收处理器
     */
    private void connect(ClientLimiterRequestExecuteHandler requestExecuteHandler) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(host, serverPort);
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("NETTY CLIENT CONNECT SUCCESS");
            } else {
                log.warn("NETTY CLIENT CONNECT FAILURE");
                lock.lock();
                try {
                    condition.signal();
                } catch (Exception e) {
                    log.warn("NETTY CLIENT CONNECT SIGNAL EXCEPTION ", e);
                } finally {
                    lock.unlock();
                }
            }
        });
        //处理channel相关
        ClientChannel.init(clientId, channelFuture.channel(), requestExecuteHandler);
        channelFuture.channel().closeFuture().sync();
    }

    /**
     * 异步启动客户端
     */
    @Async
    @Override
    public void run(String... args) throws Exception {
        run();
    }

    /**
     * netty client reconnect thread
     * 重连，由线程池维护，因sync()本身阻塞，所以采取循环处理
     * 包含重连间隔处理，重连次数
     */
    private void reconnect(ClientLimiterRequestExecuteHandler requestExecuteHandler) {
        lock.lock();
        try {
            condition.await();
        } catch (Exception e) {
            log.error("NETTY CLIENT RECONNECT AWAIT EXCEPTION ", e);
        } finally {
            lock.unlock();
        }
        long interval = reconnectInterval * 1_000_000_000L;
        final AtomicInteger upperLimit = new AtomicInteger(0);
        for (; ; ) {
            //重连间隔
            if (upperLimit.get() <= reconnectUpperLimit) {
                LockSupport.parkNanos(interval);
            } else {
                LockSupport.parkNanos(interval * 2);
            }
            initBootStrap(EventLoopGroupBuilder.build(), requestExecuteHandler);
            ChannelFuture channelFuture = bootstrap.connect(host, serverPort);
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    upperLimit.set(0);
                    log.info("NETTY CLIENT RECONNECT SUCCESS");
                } else {
                    int i = upperLimit.incrementAndGet();
                    log.warn("NETTY CLIENT RECONNECT FAILURE TIMES [{}]", i);
                }
            });
            ClientChannel.init(clientId, channelFuture.channel(), requestExecuteHandler);
            try {
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("NETTY CLIENT RECONNECT EXCEPTION ", e);
            } finally {
                childGroup.shutdownGracefully();
            }
        }
    }

    private final class Worker implements Runnable {

        final ClientLimiterRequestExecuteHandler requestExecuteHandler;

        public Worker(ClientLimiterRequestExecuteHandler requestExecuteHandler) {
            this.requestExecuteHandler = requestExecuteHandler;
        }

        @Override
        public void run() {
            reconnect(requestExecuteHandler);
        }
    }

    private final class NettyClientChannelInitializer extends ChannelInitializer {

        private final ClientLimiterRequestExecuteHandler requestExecuteHandler;

        public NettyClientChannelInitializer(ClientLimiterRequestExecuteHandler requestExecuteHandler) {
            this.requestExecuteHandler = requestExecuteHandler;
        }

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
            ch.pipeline().addLast(new KryoSerializerDecoder());
            ch.pipeline().addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
            ch.pipeline().addLast(heartbeatHandler);
            //out
            ch.pipeline().addLast(new ClientLimiterEncoderAbstract());
            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                @Override
                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                    log.info("Client 客户端重连...");
                    lock.lock();
                    try {
                        condition.signal();
                    } catch (Exception e) {
                        log.warn("NETTY CLIENT CONNECT SIGNAL EXCEPTION ", e);
                    } finally {
                        lock.unlock();
                    }
                }
            });
            ch.pipeline().addLast(requestExecuteHandler);
        }
    }
}
