package com.example.limiter.netty.remote;

public class ClientLimiterRequest {


    private Object object;

    private String clientId;

    private String reqId;


    public ClientLimiterRequest(Object object,String clientId,String reqId) {
        this.object = object;
        this.clientId = clientId;
        this.reqId = reqId;
    }

    public ClientLimiterRequest() {
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }
}
