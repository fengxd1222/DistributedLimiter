package com.example.limiter.netty;

import com.example.limiter.limiter.LimiterInfo;
import com.example.limiter.netty.handler.ClientLimiterRequestExecuteHandler;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.remote.ClientLimiterResponse;
import io.netty.channel.*;
import org.slf4j.Logger;
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

    public static boolean tryAccess() {
        String methodKey = getMethodKey();

        try {
            ClientLimiterResponse response = writeAndResponse(new LimiterInfo(methodKey));
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

    @SuppressWarnings("unchecked")
    public static <T> T write(Object object) throws ExecutionException, InterruptedException, TimeoutException {
        Assert.notNull(object, "parameter cannot be null");
        final SettableListenableFuture<ClientLimiterResponse> responseFuture = new SettableListenableFuture<>();
        String reqId = UUID.randomUUID().toString();
        baseHandler.put(reqId,responseFuture);
        ChannelFuture channelFuture = channel.writeAndFlush(new ClientLimiterRequest(object, clientId,reqId));
        System.out.println("---------");
        ClientLimiterResponse response = responseFuture.get();
        baseHandler.remove(reqId);
        return (T) response;
//        return (T) responseFuture.get(500, TimeUnit.MILLISECONDS);
    }


    private static String getMethodKey() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        String methodName = stackTraceElement.getMethodName();
        String className = stackTraceElement.getClassName();
        String methodKey = className + "." + methodName;
        return methodKey;
    }
}
