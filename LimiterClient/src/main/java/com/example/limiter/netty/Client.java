package com.example.limiter.netty;

import com.example.limiter.netty.handler.ClientLimiterRequestExecuteHandler;
import com.example.limiter.netty.handler.HeartbeatHandler;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.serializer.AbstractKryoPoolSerializerFactory;
import com.example.limiter.netty.util.ClientConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author feng xud
 */
public class Client {
    @Value("${netty.server.port}")
    public int serverPort;

    @Value("${netty.server.host}")
    public String host;

    @Value("${netty.client.id}")
    public String clientId;

    /**
     * 重连间隔
     */
    @Value("${netty.client.reconnect.interval}")
    public int reconnectInterval;

    /**
     * 重连上限 达到上限后，开始停歇双倍重连间隔
     */
    @Value("${netty.client.reconnect.upper.limit}")
    public int reconnectUpperLimit;

    final ByteBuf HEARTBEAT_BYTEBUF;
    /**
     * 心跳检测
     */
    final HeartbeatHandler heartbeatHandler;

    final ClientLimiterRequestExecuteHandler requestExecuteHandler;

    /**
     * 用来进行重连的后台线程所在的线程池
     */
    static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1), new CustomizableThreadFactory(ClientConstant.THREAD_NAME));

    public Client(ClientLimiterRequestExecuteHandler requestExecuteHandler) {
        ClientLimiterRequest HEARTBEAT = new ClientLimiterRequest();
        HEARTBEAT.setClientId(clientId);
        HEARTBEAT.setObject(ClientConstant.HEARTBEAT_STRING);
        HEARTBEAT.setReqId(ClientConstant.HEARTBEAT_STRING);
        this.HEARTBEAT_BYTEBUF = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(AbstractKryoPoolSerializerFactory.serialize(HEARTBEAT)));
        this.requestExecuteHandler = requestExecuteHandler;
        this.heartbeatHandler = new HeartbeatHandler(HEARTBEAT_BYTEBUF,requestExecuteHandler);
    }
}
