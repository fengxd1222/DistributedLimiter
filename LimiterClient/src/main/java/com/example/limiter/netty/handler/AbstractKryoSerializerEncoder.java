package com.example.limiter.netty.handler;

import com.example.limiter.netty.serializer.AbstractKryoPoolSerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;

/**
 * @author Feng xud
 */
public abstract class AbstractKryoSerializerEncoder<T> extends MessageToByteEncoder<T> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AbstractKryoSerializerEncoder.class);
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, T t, ByteBuf byteBuf) throws Exception {
        log.info("KryoSerializerEncoder "+t);
        byte[] serialize = AbstractKryoPoolSerializerFactory.serialize(t);
        byteBuf.writeBytes(serialize);
        channelHandlerContext.flush();
    }
}
