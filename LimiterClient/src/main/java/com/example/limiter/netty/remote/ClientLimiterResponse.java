
package com.example.limiter.netty.remote;
/**
 * @author feng xud
 */
public class ClientLimiterResponse {
    private Object object;

    private String reqId;

    public ClientLimiterResponse(Object object,String reqId) {
        this.object = object;
        this.reqId = reqId;
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
}
