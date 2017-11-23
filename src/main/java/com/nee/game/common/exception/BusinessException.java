package com.nee.game.common.exception;


import com.nee.game.common.constant.ErrorCodeEnum;

/**
 * exception class
 */
public class BusinessException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int errorCode;


    public BusinessException() {
        super();
    }

    /** Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     *
     * @param message error msg
     * @param errorCode error code
     */
    public BusinessException(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     *
     * @param errorCodeEnum error code enum
     */
    public BusinessException(ErrorCodeEnum errorCodeEnum) {
        super (errorCodeEnum.getMessage());
        this.errorCode = errorCodeEnum.getCode();
    }

    /**
     *
     * @param errorCodeEnum error code enum
     * @param args format argument
     */
    public BusinessException(ErrorCodeEnum errorCodeEnum, Object... args) {
        super (String.format(errorCodeEnum.getMessage(), args));
        this.errorCode = errorCodeEnum.getCode();
    }

    /**
     *
     * @param errorCodeEnum error code enum
     */
    public BusinessException(String message, ErrorCodeEnum errorCodeEnum) {
        super (message);
        this.errorCode = errorCodeEnum.getCode();
    }

    /**
     *
     * @param message msg
     * @param errorCodeEnum error code enum
     * @param args format argument
     */
    public BusinessException(String message, ErrorCodeEnum errorCodeEnum, Object... args) {
        super (String.format(message, args));
        this.errorCode = errorCodeEnum.getCode();
    }

    public String getErrorCode() {
        return errorCode + "";
    }
}