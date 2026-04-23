package com.itbaizhan.shopping_common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回状态码枚举类
 */
@Getter
@AllArgsConstructor
public enum CodeEnum {
    // 正常
    SUCCESS(200, "OK"),
    //异常
    SYSTEM_ERROR(500, "服务器异常"),
    // 业务异常
    PARAMETER_ERROR(601, "参数异常"),
    //添加商品类型异常
    INSERT_PRODUCT_TYPE_ERROR(602, "3级商品类型不能添加子类型"),
    //删除商品类型异常
    DELETE_PRODUCT_TYPE_ERROR(603, "该商品类型有子类型，不能删除")
    ;

    private final Integer code;
    private final String message;
}
