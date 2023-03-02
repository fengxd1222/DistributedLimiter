package com.example.limiter.netty.util;

import com.example.limiter.netty.handler.ClientLimiterEncoder;
import com.example.limiter.netty.handler.ClientLimiterResponseEncoder;
import com.example.limiter.netty.handler.KryoSerializerDecoder;
import com.example.limiter.netty.handler.LimiterHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


public class EventLoopGroupBuilder {

    static String os;
    static {
        os = System.getProperty("os.name");
    }

    public Group<MultithreadEventLoopGroup, MultithreadEventLoopGroup> build() {
        if (os != null && os.toLowerCase().startsWith("linux")) {//Linux操作系统 使用epoll
            return new Group<>(new EpollEventLoopGroup(1), new EpollEventLoopGroup(10),true);
        } else {
            return new Group<>(new NioEventLoopGroup(1), new NioEventLoopGroup(10),false);
        }
    }

    public static ChannelHandler[] channelHandlers(){
        return new ChannelHandler[]{
                new KryoSerializerDecoder(),
                new LoggingHandler(LogLevel.DEBUG),
                new ClientLimiterEncoder(),
                new ClientLimiterResponseEncoder(),
                new LimiterHandler()
        };
    }

    public static void addHandler(ServerBootstrap serverBootstrap){
        if (os != null && os.toLowerCase().startsWith("linux")) {
            serverBootstrap.handler(new ChannelInitializer<EpollServerSocketChannel>(){
                @Override
                protected void initChannel(EpollServerSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(channelHandlers());
                }
            });
        }else {
            serverBootstrap.handler(new ChannelInitializer<NioServerSocketChannel>(){
                @Override
                protected void initChannel(NioServerSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(channelHandlers());
                }
            });
        }
    }
    public static void addChildHandler(ServerBootstrap serverBootstrap){
        if (os != null && os.toLowerCase().startsWith("linux")) {
            serverBootstrap.childHandler(new ChannelInitializer<EpollSocketChannel>(){
                @Override
                protected void initChannel(EpollSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(channelHandlers());
                }
            });
        }else {
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>(){
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(channelHandlers());
                }
            });
        }
    }


    public class Group<P, C> {
        private MultithreadEventLoopGroup P;
        private MultithreadEventLoopGroup C;

        private Class<? extends ServerSocketChannel> channelClass;

        public Group(MultithreadEventLoopGroup p, MultithreadEventLoopGroup c,boolean isLinux) {
            P = p;
            C = c;
            if(isLinux){
                this.channelClass = EpollServerSocketChannel.class;
            }else {
                this.channelClass = NioServerSocketChannel.class;
            }
        }

        public MultithreadEventLoopGroup getP() {
            return P;
        }

        public MultithreadEventLoopGroup getC() {
            return C;
        }

        public Class<? extends ServerSocketChannel> getChannelClass() {
            return channelClass;
        }
    }
}
