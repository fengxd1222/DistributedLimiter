package com.example.limiter.netty.handler;

import com.example.limiter.limiter.strategy.ChannelReadHolder;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.remote.ClientLimiterResponse;
import com.example.limiter.netty.util.ClientConstant;
import com.example.limiter.netty.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

/**
 * @author feng xud
 */
public class AuthorizationHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AuthorizationHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof ClientLimiterRequest){
            ClientLimiterRequest clientLimiterRequest = (ClientLimiterRequest) msg;
            //心跳不校验token
            if(ClientConstant.HEARTBEAT_STRING.equals(clientLimiterRequest.getObject())){
                log.info("AuthorizationHandler "+ClientConstant.HEARTBEAT_STRING);
                ctx.writeAndFlush(new ClientLimiterResponse("OK",clientLimiterRequest.getReqId()));
                return;
            }
            //优先判断缓存是否有token，没有就进行token验证，并放入缓存
            String checkout = ChannelReadHolder.getTokenCacheHandler().checkout(clientLimiterRequest.getToken());
            Claims claims = null;
            if(checkout==null && (claims=JwtUtils.parseToken(clientLimiterRequest.getToken()))==null){
                //缓存没有 同时token解析失败，返回授权失败的信息
                ctx.writeAndFlush(new ClientLimiterResponse("Authorization Failed",clientLimiterRequest.getReqId(),"Authorization Failed", HttpStatus.UNAUTHORIZED.value()));
                return;
            }
            ChannelReadHolder.getTokenCacheHandler().putToken(clientLimiterRequest.getToken(), (String) claims.get("userId"),claims.getExpiration().getTime());
        }
        ctx.fireChannelRead(msg);
    }
}
