package com.nee.game.common;

/**
 * Created by heikki on 17/8/24.
 */
public class Request<T> {


    /** 签名 */
    private String sign;
    /** 时间戳 */
    private String timestamp;
    /** 请求方法 */
    private int cmd;
    /** 版本号 */
    private String version;

    /** 请求业务参数，请参看具体api说明 */
    private T params;

    public String getSign() {
        return sign;
    }

    public Request setSign(String sign) {
        this.sign = sign;
        return this;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Request setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public T getParams() {
        return params;
    }

    public Request setParams(T params) {
        this.params = params;
        return this;
    }
}



