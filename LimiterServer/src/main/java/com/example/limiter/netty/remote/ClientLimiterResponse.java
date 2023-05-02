package com.example.limiter.netty.remote;

public class ClientLimiterResponse {
    private Object object;

    private String reqId;

    private String message;

    private int errorCode;


    public ClientLimiterResponse(Object object, String reqId) {
        this.object = object;
        this.reqId = reqId;
    }

    public ClientLimiterResponse(Object object, String reqId, String message, int errorCode) {
        this.object = object;
        this.reqId = reqId;
        this.message = message;
        this.errorCode = errorCode;
    }


    public ClientLimiterResponse() {
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

}
