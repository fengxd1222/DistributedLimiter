package com.example.limiter.netty.util;

import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author feng xud
 */
public class EventLoopGroupBuilder {

    static String os;
    private static final String LINUX = "linux";
    static {
        os = System.getProperty("os.name");
    }
    public static Group<MultithreadEventLoopGroup> build() {
        //Linux操作系统 使用epoll
        if (os != null && os.toLowerCase().startsWith(LINUX)) {
            return new Group<>(new EpollEventLoopGroup(10), true);
        } else {
            return new Group<>(new NioEventLoopGroup(10), false);
        }
    }

    public static class Group<C> {

        private MultithreadEventLoopGroup multithreadEventLoopGroup;

        private Class<? extends SocketChannel> channelClass;

        public Group(MultithreadEventLoopGroup c, boolean isLinux) {
            multithreadEventLoopGroup = c;
            if (isLinux) {
                this.channelClass = EpollSocketChannel.class;
            } else {
                this.channelClass = NioSocketChannel.class;
            }
        }

        public MultithreadEventLoopGroup getC() {
            return multithreadEventLoopGroup;
        }

        public Class<? extends SocketChannel> getChannelClass() {
            return channelClass;
        }
    }
}
