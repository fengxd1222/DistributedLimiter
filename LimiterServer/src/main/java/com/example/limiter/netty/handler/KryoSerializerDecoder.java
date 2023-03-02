package com.example.limiter.netty.handler;

import com.example.limiter.netty.serializer.KryoPoolSerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;

import java.util.List;

public class KryoSerializerDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(KryoSerializerDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        Object deserialize = KryoPoolSerializerFactory.deserialize(byteBuf);
        if (deserialize == null) return;
        log.info("KryoSerializerDecoder: " + deserialize);
        list.add(deserialize);
    }
}
