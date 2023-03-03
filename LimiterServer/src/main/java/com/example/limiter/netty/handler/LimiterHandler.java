package com.example.limiter.netty.handler;


import com.example.limiter.limiter.strategy.ChannelReadHolder;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.remote.ClientLimiterResponse;
import com.example.limiter.netty.util.ClientConstant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;

public class LimiterHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LimiterHandler.class);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("服务端建立连接...");
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ClientLimiterRequest) {
            ClientLimiterRequest clientLimiterRequest = (ClientLimiterRequest) msg;
            Object object = clientLimiterRequest.getObject();
            if(ClientConstant.HEARTBEAT_STRING.equals(object)){
                ctx.writeAndFlush(new ClientLimiterResponse("OK",clientLimiterRequest.getReqId()));
            }else {
                Object res = ChannelReadHolder.handle(object, clientLimiterRequest.getClientId());
                ctx.writeAndFlush(new ClientLimiterResponse(res,clientLimiterRequest.getReqId()));
                log.info(" res " + res);
            }
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught...",cause);
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.error("channelInactive...");
        ctx.fireChannelInactive();
    }
}
