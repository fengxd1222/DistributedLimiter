package com.example.limiter.netty.handler;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

public class ClientLimiterEncoder extends KryoSerializerEncoder<ClientLimiterRequest> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ClientLimiterEncoder.class);
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ClientLimiterRequest clientLimiter, ByteBuf byteBuf) throws Exception {
        log.info("ClientLimiterEncoder: "+clientLimiter);
        super.encode(channelHandlerContext, clientLimiter, byteBuf);
    }
}
