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
    DELETE_PRODUCT_TYPE_ERROR(603, "该商品类型有子类型，不能删除"),
    // 文件上传异常
    UPLOAD_FILE_ERROR(604,"文件上传异常"),
    //注册输入的验证码错误
    REGISTER_CODE_ERROR(605,"注册验证码错误"),
    //注册输入的手机号重复
    REGISTER_REPEAT_PHONE_ERROR(606,"注册手机号重复"),
    //注册输入的用户名重复
    REGISTER_REPEAT_NAME_ERROR(607,"注册用户名重复"),
    //登录输入的用户名或密码错误
    LOGIN_NAME_PASSWORD_ERROR(608,"登录用户名或密码错误"),
    //登录输入的验证码错误
    LOGIN_CODE_ERROR(609,"登录验证码错误"),
    //用户未注册
    LOGIN_NOPHONE_ERROR(610,"用户未注册"),
    //用户状态异常
    LOGIN_USER_STATUS_ERROR(611,"用户状态异常"),
    //二维码生成异常
    QR_CODE_ERROR(612,"二维码生成异常"),
    //支付宝验签异常
    CHECK_SIGN_ERROR(613,"支付宝验签异常"),
    //订单不能重复支付(状态异常)
    ORDER_STATUS_ERROR(614,"订单状态异常，不能重复支付"),
    //登录输入的验证码过期
    LOGIN_CODE_EXPIRE(614,"登录验证码过期"),
    ;

    private final Integer code;
    private final String message;
}
