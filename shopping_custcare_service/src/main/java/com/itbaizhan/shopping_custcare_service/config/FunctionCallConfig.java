package com.itbaizhan.shopping_custcare_service.config;

import com.itbaizhan.shopping_custcare_service.functioncall.GoodsQueryService;
import com.itbaizhan.shopping_custcare_service.functioncall.OrderQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class FunctionCallConfig {
    @Bean
    //@Description核心作用是告诉 AI 模型何时以及如何调用它,以及一些参数的信息
    /*告诉 AI 模型何时调用该函数 - 通过描述函数的功能
说明如何调用 - 包括参数信息（订单状态、查询条数）
参数来源说明 - 用户ID从prompt获取*/
    @Description("查询当前用户的订单信息，输入订单状态（1=待支付，2=已完成）和查询条数（默认为1），用户ID从prompt获取")
    public Function<OrderQueryService.Request,OrderQueryService.Response> getOrderQueryService(){
        return new OrderQueryService();
    }
    @Bean
    @Description("查询商品信息，输入商品关键字和查询条数（默认为1）")
    public Function<GoodsQueryService.Request,GoodsQueryService.Response> getGoodsQueryService() {
        return new GoodsQueryService();
    }
}

