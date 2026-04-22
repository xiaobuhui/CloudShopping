package com.itbaizhan.shopping_manager_api.handle;

import com.alibaba.fastjson2.JSON;
import com.itbaizhan.shopping_common.result.BaseResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

// 登录成功处理器
public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        BaseResult result = new BaseResult(200,"登录成功",null);
        response.setContentType("text/json;charset=utf-8");
        //返回前端登录结果，前端来决定是跳转还是页面弹窗等操作
        response.getWriter().write(JSON.toJSONString(result));
    }
}
