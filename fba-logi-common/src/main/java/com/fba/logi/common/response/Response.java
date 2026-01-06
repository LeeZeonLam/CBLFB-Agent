package com.fba.logi.common.response;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应封装
 *
 * @param <T> 响应数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private String code;

    /**
     * 响应信息
     */
    private String info;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> Response<T> success() {
        return Response.<T>builder()
                .code(Constants.ResponseCode.SUCCESS)
                .info("成功")
                .build();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(Constants.ResponseCode.SUCCESS)
                .info("成功")
                .data(data)
                .build();
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> Response<T> success(String info, T data) {
        return Response.<T>builder()
                .code(Constants.ResponseCode.SUCCESS)
                .info(info)
                .data(data)
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> Response<T> fail(String code, String info) {
        return Response.<T>builder()
                .code(code)
                .info(info)
                .build();
    }

    /**
     * 失败响应（默认错误码）
     */
    public static <T> Response<T> fail(String info) {
        return Response.<T>builder()
                .code(Constants.ResponseCode.UN_ERROR)
                .info(info)
                .build();
    }

    /**
     * 参数错误响应
     */
    public static <T> Response<T> illegalParameter(String info) {
        return Response.<T>builder()
                .code(Constants.ResponseCode.ILLEGAL_PARAMETER)
                .info(info)
                .build();
    }

    /**
     * 未授权响应
     */
    public static <T> Response<T> unauthorized() {
        return Response.<T>builder()
                .code(Constants.ResponseCode.UNAUTHORIZED)
                .info("未授权")
                .build();
    }

    /**
     * 禁止访问响应
     */
    public static <T> Response<T> forbidden() {
        return Response.<T>builder()
                .code(Constants.ResponseCode.FORBIDDEN)
                .info("禁止访问")
                .build();
    }

}
