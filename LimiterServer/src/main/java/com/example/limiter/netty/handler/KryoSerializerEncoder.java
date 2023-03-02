package com.example.limiter.netty.handler;

import com.example.limiter.netty.serializer.KryoPoolSerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public abstract class KryoSerializerEncoder<T> extends MessageToByteEncoder<T> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, T t, ByteBuf byteBuf) throws Exception {
        byte[] serialize = KryoPoolSerializerFactory.serialize(t);
        byteBuf.writeBytes(serialize);
        channelHandlerContext.flush();
    }
}
