package com.example.limiter.netty;

import com.example.limiter.netty.util.EventLoopGroupBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
@Order(1)
@Component
public class LimiterServer implements CommandLineRunner {
    @Value("${netty.server.port}")
    public int serverPort;

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LimiterServer.class);

    public void run(){
        EventLoopGroupBuilder.Group<MultithreadEventLoopGroup, MultithreadEventLoopGroup> group = new EventLoopGroupBuilder().build();
        MultithreadEventLoopGroup parentGroup = group.getP();
        MultithreadEventLoopGroup childGroup = group.getC();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(parentGroup,childGroup)
                    .channel(group.getChannelClass())
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childOption(ChannelOption.TCP_NODELAY,false);
//                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());

            EventLoopGroupBuilder.addHandler(serverBootstrap);

            //ch.pipeline().addLast(new FixedLengthFrameDecoder(10));
            EventLoopGroupBuilder.addChildHandler(serverBootstrap);

            ChannelFuture channelFuture = serverBootstrap.bind(serverPort).sync();
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
            log.info("netty server closed");
        }


    }

    @PreDestroy
    public void stop(){
        //todo 需要向客户端广播 服务端即将关闭，清转成单机限流模式


    }

    @Async
    @Override
    public void run(String... args) throws Exception {
        run();
    }

}
