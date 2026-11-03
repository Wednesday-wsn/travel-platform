package com.travel.common.result;

import lombok.Getter;

/**
 * 统一返回码
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "系统异常"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),

    LOGIN_FAIL(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1002, "账号已被禁用"),
    ACCOUNT_EXISTS(1003, "账号已存在"),

    STOCK_NOT_ENOUGH(2001, "库存不足"),
    ORDER_STATUS_ERROR(2002, "订单状态异常");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
