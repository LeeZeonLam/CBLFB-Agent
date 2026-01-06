package com.fba.logi.common.exception;

import com.fba.logi.common.constants.Constants;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误信息
     */
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = Constants.ResponseCode.UN_ERROR;
        this.message = message;
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 参数错误异常
     */
    public static BusinessException illegalParameter(String message) {
        return new BusinessException(Constants.ResponseCode.ILLEGAL_PARAMETER, message);
    }

    /**
     * 未找到异常
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(Constants.ResponseCode.NOT_FOUND, message);
    }

    /**
     * 重复索引异常
     */
    public static BusinessException duplicateKey(String message) {
        return new BusinessException(Constants.ResponseCode.INDEX_DUP, message);
    }

}
