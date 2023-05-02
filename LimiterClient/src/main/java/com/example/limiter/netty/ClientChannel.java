package com.example.limiter.netty;

import com.example.limiter.limiter.LimiterInfo;
import com.example.limiter.netty.handler.ClientLimiterRequestExecuteHandler;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.remote.ClientLimiterResponse;
import io.netty.channel.*;
import org.apache.tomcat.websocket.AuthenticationException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author feng xud
 */
public class ClientChannel {
    public static Channel channel;

    private static String clientId;

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ClientChannel.class);

    private static ClientLimiterRequestExecuteHandler baseHandler;
    public static void init(String clientId, Channel channel, ClientLimiterRequestExecuteHandler requestExecuteHandler) {
        ClientChannel.channel = channel;
        ClientChannel.clientId = clientId;
        ClientChannel.baseHandler = requestExecuteHandler;
    }

    /**
     * 发送限流测试，是否允许访问
     * @return true 允许访问  false 限流
     */
    public static boolean tryAccess(String token) {
        String methodKey = getMethodKey();
        try {
            ClientLimiterResponse response = writeAndResponse(new LimiterInfo(methodKey),token);
            if(response.getErrorCode()== HttpStatus.UNAUTHORIZED.value()){
                throw new AuthenticationException(response.getMessage());
            }
            if (response.getObject()==null){
                return false;
            }
            return (boolean) response.getObject();
        } catch (Exception e) {
            log.error("tryAccess run exception : " + e);
        }
        return false;
    }



    private static ClientLimiterResponse writeAndResponse(Object object) throws ExecutionException, InterruptedException, TimeoutException {
        return write(object);
    }
    private static ClientLimiterResponse writeAndResponse(Object object,String token) throws ExecutionException, InterruptedException, TimeoutException {
        return write(object,token);
    }

    @SuppressWarnings("unchecked")
    public static <T> T write(Object object) throws ExecutionException, InterruptedException, TimeoutException {
        Assert.notNull(object, "parameter cannot be null");
        //创建一个future，异步获取响应
        final SettableListenableFuture<ClientLimiterResponse> responseFuture = new SettableListenableFuture<>();
        String reqId = UUID.randomUUID().toString();
        baseHandler.put(reqId,responseFuture);
        ChannelFuture channelFuture = channel.writeAndFlush(new ClientLimiterRequest(object, clientId,reqId));
        //阻塞等待结果，可以更改为超时
        ClientLimiterResponse response = responseFuture.get();
        //获取结果后需要处理ClientLimiterRequestExecuteHandler中的Map
        baseHandler.remove(reqId);
        return (T) response;
//        return (T) responseFuture.get(500, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    public static <T> T write(Object object,String token) throws ExecutionException, InterruptedException, TimeoutException {
        Assert.notNull(object, "parameter cannot be null");
        //创建一个future，异步获取响应
        final SettableListenableFuture<ClientLimiterResponse> responseFuture = new SettableListenableFuture<>();
        String reqId = UUID.randomUUID().toString();
        baseHandler.put(reqId,responseFuture);
        ChannelFuture channelFuture = channel.writeAndFlush(new ClientLimiterRequest(object, clientId,reqId,token));
        //阻塞等待结果，可以更改为超时
        ClientLimiterResponse response = responseFuture.get();
        //获取结果后需要处理ClientLimiterRequestExecuteHandler中的Map
        baseHandler.remove(reqId);
        return (T) response;
    }


    private static String getMethodKey() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        String methodName = stackTraceElement.getMethodName();
        String className = stackTraceElement.getClassName();
        String methodKey = className + "." + methodName;
        return methodKey;
    }
}
