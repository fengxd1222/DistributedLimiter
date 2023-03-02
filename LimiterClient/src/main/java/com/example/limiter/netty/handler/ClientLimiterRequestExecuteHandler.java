package com.example.limiter.netty.handler;

import com.example.limiter.limiter.LimiterConfig;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.remote.ClientLimiterResponse;
import com.example.limiter.netty.remote.LimiterDefinition;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author feng xud
 */
@ChannelHandler.Sharable
public class ClientLimiterRequestExecuteHandler extends SimpleChannelInboundHandler<ClientLimiterResponse> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ClientLimiterRequestExecuteHandler.class);

    private String clientId;
    private List<LimiterDefinition> limiterDefinitions;
    private final ConcurrentHashMap<String,SettableListenableFuture<ClientLimiterResponse>> FUTURE_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();


    public ClientLimiterRequestExecuteHandler(List<LimiterDefinition> limiterDefinitions, String clientId) {
        this.limiterDefinitions = limiterDefinitions;
        this.clientId = clientId;
    }
    public ClientLimiterRequestExecuteHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientLimiterResponse msg) throws Exception {
        FUTURE_CONCURRENT_HASH_MAP.get(msg.getReqId()).set(msg);
    }


    public void put(String reqId,SettableListenableFuture<ClientLimiterResponse> future){
        FUTURE_CONCURRENT_HASH_MAP.put(reqId, future);
        System.out.println("Size : "+FUTURE_CONCURRENT_HASH_MAP.size());
    }

    public void remove(String reqId){
        FUTURE_CONCURRENT_HASH_MAP.remove(reqId);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String reqId = UUID.randomUUID().toString();
        ClientLimiterRequest clientLimiterRequest = new ClientLimiterRequest(new LimiterConfig(limiterDefinitions), clientId,reqId);
        put(reqId,new SettableListenableFuture<>());
        ctx.writeAndFlush(clientLimiterRequest);
        log.info("client send clientLimiterRequest: " + clientLimiterRequest);
        ctx.fireChannelActive();
    }
}
