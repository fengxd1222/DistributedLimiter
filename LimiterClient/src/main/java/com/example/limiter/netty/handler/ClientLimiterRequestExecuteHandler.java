package com.example.limiter.netty.handler;

import com.example.limiter.limiter.LimiterConfig;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.remote.ClientLimiterResponse;
import com.example.limiter.netty.remote.LimiterDefinition;
import com.example.limiter.netty.util.ClientConstant;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author feng xud
 */
@ChannelHandler.Sharable
@Component
public class ClientLimiterRequestExecuteHandler extends SimpleChannelInboundHandler<ClientLimiterResponse> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ClientLimiterRequestExecuteHandler.class);

    /**
     * 当前客户端id
     */
    private String clientId;
    /**
     * 当前客户端所有的限流配置
     */
    private List<LimiterDefinition> limiterDefinitions;
    /**
     * 当前正在异步等待获取响应值的Future类
     */
    private final ConcurrentHashMap<String,SettableListenableFuture<ClientLimiterResponse>> FUTURE_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();


    public ClientLimiterRequestExecuteHandler(List<LimiterDefinition> limiterDefinitions, String clientId) {
        this.limiterDefinitions = limiterDefinitions;
        this.clientId = clientId;
    }
    public ClientLimiterRequestExecuteHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientLimiterResponse msg) throws Exception {
        if(ClientConstant.HEARTBEAT_STRING.equals(msg.getReqId())){
            return;
        }
        FUTURE_CONCURRENT_HASH_MAP.get(msg.getReqId()).set(msg);
    }


    public void put(String reqId,SettableListenableFuture<ClientLimiterResponse> future){
        FUTURE_CONCURRENT_HASH_MAP.put(reqId, future);
    }

    public void remove(String reqId){
        FUTURE_CONCURRENT_HASH_MAP.remove(reqId);
    }

    /**
     * 连接成功时，向服务端发送本客户端的限流器配置
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String reqId = UUID.randomUUID().toString();
        ClientLimiterRequest clientLimiterRequest = new ClientLimiterRequest(new LimiterConfig(limiterDefinitions), clientId,reqId);
        put(reqId,new SettableListenableFuture<>());
        ctx.writeAndFlush(clientLimiterRequest);
        log.info("client send clientLimiterRequest: " + clientLimiterRequest);
        ctx.fireChannelActive();
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<LimiterDefinition> getLimiterDefinitions() {
        return limiterDefinitions;
    }

    public void setLimiterDefinitions(List<LimiterDefinition> limiterDefinitions) {
        this.limiterDefinitions = limiterDefinitions;
    }
}
