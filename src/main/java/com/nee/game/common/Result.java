package com.nee.game.common;

import com.nee.game.uitls.StringUtils;

/**
 * Created by heikki on 17/8/20.
 */
public class Result {

    /** 请求id */
    private Integer cmd;
    /** 返回结果编码 */
    private String code;
    /** 返回结果消息 */
    private String message;
    /** 返回数据对象 */
    private Object data;

    public static class Builder {
        /** 请求id */
        private Integer cmd;
        /** 返回结果编码 */
        private String code;
        /** 返回结果消息 */
        private String message;
        /** 返回数据对象 */
        private Object data;

        public Builder(){}

        public Builder(Integer cmd){
            this.cmd = cmd;
        }

        public Builder setCmd(Integer cmd) {
            this.cmd = cmd;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setData(Object data) {
            this.data = data;
            return this;
        }

        public Result build() { return new Result(this);}
    }

    public Integer getCmd() {
        return cmd;
    }

    public String getCode() {
        return code == null ? "0" : code;
    }

    public String getMessage() {
        return StringUtils.isBlank(message)? "操作成功" : message;
    }

    public Object getData() {
        return data;
    }

    private Result(Builder builder) {
        this.cmd = builder.cmd;
        this.message = builder.message;
        this.data = builder.data;
        this.code = builder.code;
    }
}