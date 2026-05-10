package com.itbaizhan.shopping_custcare_customer_api.controller;

import com.itbaizhan.shopping_common.pojo.ShoppingUser;
import com.itbaizhan.shopping_common.result.BaseResult;
import com.itbaizhan.shopping_common.service.AICustCareService;
import com.itbaizhan.shopping_common.service.ShoppingUserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

/**
 * AI客服
 */
@RestController
@RequestMapping("/user/custcare")
public class AICustCareController {
    @DubboReference(timeout = 30000) // 指定30秒超时
    private AICustCareService aiCustCareService;

    @DubboReference
    private ShoppingUserService shoppingUserService;
    /**
     * 回答用户问题
     * @param message 问题
     * @return 答案
     */
    @GetMapping("/answer")
    public BaseResult<String> answer(@RequestParam("message") String message, @RequestHeader("Authorization") String authorization){
      /*  从请求头 Authorization 中提取 Bearer token。
        通过 shoppingUserService.getLoginUser(token) 解析用户 ID（如果解析失败则 userId 为 null，异常被吃掉）。*/
        String token = authorization.replace("Bearer ","");
        Long userId = null;
        try {
            ShoppingUser loginUser = shoppingUserService.getLoginUser(token);
            userId = loginUser.getId();
        }catch (Exception e){}
        String answer = aiCustCareService.generateResponse(message,userId);
        return BaseResult.ok(answer);
    }
}
