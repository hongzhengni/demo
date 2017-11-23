package com.nee.game.common.constant;

/**
 * Created by heikki on 17/5/8.
 */
public enum ErrorCodeEnum {

    /**
     * 操作成功
     */
    SUCCESS(0, "操作成功"),
    /**
     * 系统错误，稍后再试
     */
    SYSTEM_ERROR(1001, "系统错误，稍后再试 "),
    /**
     * 参数为空
     */
    NO_PARAM(1002, "参数为空"),
    /**
     * 新增的参数已经存在(唯一性约束)
     */
    DUPLICATE_DATA(1003, "新增的参数已经存在(唯一性约束)"),
    /**
     * 参数不正确
     */
    ERROR_PARAM(1004, "参数不正确"),
    /**
     * 逻辑错误
     */
    ERROR_LOGIC(1005, "逻辑错误"),
    /**
     * 用户认证失败
     */
    AUTH_ERROR(1007, "用户认证失败"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXIST(1011, "数据不存在"),
    /**
     * 用户未激活
     */
    USER_NOT_ACTIVE(1012, "用户未激活"),
    /**
     * 用户已注销
     */
    LOGOUT_USER(1013, "用户已注销"),
    REQUEST_ERROR(1017, "没有相应的方法"),
    /**
     * 版本号不正确
     */
    VERSION_ERROR(1018, "版本号不正确"),;

    private int code;
    private String message;

    private ErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
