package com.itbaizhan.shopping_manager_api.handle;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 统一异常处理器
//全局统一异常处理 + 全局数据绑定 + 全局数据预处理
@RestControllerAdvice
public class AccessDeniedExceptionHandler {
    // 处理权限不足异常，捕获到异常后再次抛出，交给AccessDeniedHandler处理
    @ExceptionHandler(AccessDeniedException.class)
    public void defaultExceptionHandler(AccessDeniedException e) throws AccessDeniedException{
        throw e;
    }
}
