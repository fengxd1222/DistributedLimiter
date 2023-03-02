package com.example.limiter.netty;

import com.example.limiter.limiter.LimiterScanner;
import com.example.limiter.netty.handler.ClientLimiterEncoderAbstract;
import com.example.limiter.netty.handler.ClientLimiterRequestExecuteHandler;
import com.example.limiter.netty.handler.KryoSerializerDecoder;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.remote.LimiterDefinition;
import com.example.limiter.netty.util.EventLoopGroupBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * @author feng xud
 */
@Component
@Order(1)
public class LimiterNettyClient implements CommandLineRunner {

    @Value("${netty.server.port}")
    public int serverPort;

    @Value("${netty.server.host}")
    public String host;

    @Value("${netty.client.id}")
    public String clientId;


    private Bootstrap bootstrap;

    private EventLoopGroup childGroup;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LimiterNettyClient.class);



    private static final LimiterScanner SCANNER = new LimiterScanner();

    public void run() {
        List<LimiterDefinition> limiterDefinitions = SCANNER.doScan();
        EventLoopGroupBuilder.Group<MultithreadEventLoopGroup> group = EventLoopGroupBuilder.build();
        final ClientLimiterRequestExecuteHandler requestExecuteHandler = new ClientLimiterRequestExecuteHandler(limiterDefinitions, clientId);
        childGroup = group.getC();
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(childGroup)
                    .channel(group.getChannelClass())
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            ch.pipeline().addLast(new KryoSerializerDecoder());
//                            ch.pipeline().addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
//                            ch.pipeline().addLast(new HeartbeatHandler(LimiterNettyClient.this.clientId));
                            //out
                            ch.pipeline().addLast(new ClientLimiterEncoderAbstract());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("Client 客户端重连...");
                                    connect(requestExecuteHandler);
                                }
                            });
                            ch.pipeline().addLast(requestExecuteHandler);

                        }

                    });
            connect(requestExecuteHandler);
        } catch (Exception e) {
            log.error("client error ", e);
        } finally {
            childGroup.shutdownGracefully();
        }
    }

    private void connect(ClientLimiterRequestExecuteHandler requestExecuteHandler) throws InterruptedException {
        AtomicBoolean isSuccess = new AtomicBoolean(false);
        ChannelFuture channelFuture = bootstrap.connect(host, serverPort);
        channelFuture.addListener((ChannelFutureListener) future -> {
            final EventLoop loop = future.channel().eventLoop();
            ScheduledFuture<?> scheduledFuture = loop.scheduleAtFixedRate(() -> {
                log.info("连接正在重试...");
                try {
                    if (future.isSuccess()) {
                        isSuccess.set(false);
                        log.info("连接Netty服务端成功...");
                    } else {
                        isSuccess.set(true);
                        connect(requestExecuteHandler);
                        log.info("连接Netty服务端失败，进行断线重连...");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, 10, 20, TimeUnit.SECONDS);

        });
        ClientChannel.init(clientId, channelFuture.channel(),requestExecuteHandler);
        channelFuture.channel().closeFuture().sync();
    }

    @Async
    @Override
    public void run(String... args) throws Exception {
        run();
    }

    private static class HeartbeatHandler extends ChannelInboundHandlerAdapter {

        private static final ClientLimiterRequest HEARTBEAT = new ClientLimiterRequest();

        public HeartbeatHandler(String clientId) {
            HEARTBEAT.setClientId(clientId);
            HEARTBEAT.setObject("HEARTBEAT");
        }
//        private static final ByteBuf HEARTBEAT = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("HEARTBEAT", Charset.defaultCharset()));

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                log.info("HeartbeatHandler...心跳");
                ctx.writeAndFlush(HEARTBEAT).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                ctx.fireUserEventTriggered(evt);
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}
