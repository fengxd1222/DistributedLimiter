package com.example.limiter.netty.handler;

import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.remote.ClientLimiterResponse;
import com.example.limiter.netty.serializer.AbstractKryoPoolSerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.UUID;

/**
 * @author feng xud
 */
@ChannelHandler.Sharable
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HeartbeatHandler.class);
    private final ByteBuf HEARTBEAT_BYTEBUF;

    private final ClientLimiterRequestExecuteHandler requestExecuteHandler;

    private static final String HEARTBEAT_HANDLER_HEARTBEAT = "HeartbeatHandler HEARTBEAT";
    public HeartbeatHandler(ByteBuf HEARTBEAT_BYTEBUF,ClientLimiterRequestExecuteHandler requestExecuteHandler) {
        this.HEARTBEAT_BYTEBUF = HEARTBEAT_BYTEBUF;
        this.requestExecuteHandler = requestExecuteHandler;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            log.info(HEARTBEAT_HANDLER_HEARTBEAT);
            ctx.writeAndFlush(HEARTBEAT_BYTEBUF).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            ctx.fireUserEventTriggered(evt);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
