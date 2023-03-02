package com.example.limiter.netty.handler;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.remote.ClientLimiterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

public class ClientLimiterResponseEncoder extends KryoSerializerEncoder<ClientLimiterResponse> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ClientLimiterResponseEncoder.class);
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ClientLimiterResponse clientLimiter, ByteBuf byteBuf) throws Exception {
        log.info("ClientLimiterResponseEncoder: "+clientLimiter);
        super.encode(channelHandlerContext, clientLimiter, byteBuf);
    }
}
