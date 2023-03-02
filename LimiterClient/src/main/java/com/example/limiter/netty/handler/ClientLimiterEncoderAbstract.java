package com.example.limiter.netty.handler;

import com.example.limiter.netty.remote.ClientLimiterRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

/**
 * @author feng xud
 */
public class ClientLimiterEncoderAbstract extends AbstractKryoSerializerEncoder<ClientLimiterRequest> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ClientLimiterEncoderAbstract.class);
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ClientLimiterRequest clientLimiterRequest, ByteBuf byteBuf) throws Exception {
        log.info("ClientLimiterEncoder: "+ clientLimiterRequest);
        super.encode(channelHandlerContext, clientLimiterRequest, byteBuf);
    }
}
