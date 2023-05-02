package com.example.limiter.netty.handler;

import com.example.limiter.netty.serializer.KryoPoolSerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;

public abstract class KryoSerializerEncoder<T> extends MessageToByteEncoder<T> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(KryoSerializerEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, T t, ByteBuf byteBuf) throws Exception {
        log.info("KryoSerializerEncoder: " + t);
        byte[] serialize = KryoPoolSerializerFactory.serialize(t);
        byteBuf.writeBytes(serialize);
        channelHandlerContext.flush();
    }
}
