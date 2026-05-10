package com.itbaizhan.shopping_custcare_service.functioncall;

import com.itbaizhan.shopping_common.pojo.Orders;
import com.itbaizhan.shopping_common.service.OrdersService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
//它实现了 java.util.function.Function<OrderQueryService.Request, OrderQueryService.Response>，
// 意味着它的核心逻辑就是输入一个 Request 对象，输出一个 Response 对象，
@Component
public class OrderQueryService implements Function<OrderQueryService.Request,OrderQueryService.Response> {
    @DubboReference
    private OrdersService ordersService;
    /**
     * 应用
     * @param
     * @return
     */
    @Override
    public Response apply(Request request) {
        Long userId = request.userId();
        Integer status = request.status();
        int size = request.size() != null ? request.size() : 1; // 默认 1 条
        List<Orders> orders = ordersService.findUserOrders(userId,status);
        List<Orders> subList = orders.subList(0, size < orders.size() ? size : orders.size());
        return new Response(subList);
    }
    /**
     * 输入
     * @param userId 用户ID
     * @param size 查询条数
     * @param status 订单状态
     */
    public record Request(Long userId , Integer size,Integer status){}
    /**
     * 输出
     * @param orders 订单信息集合
     */
    public record Response(List<Orders> orders){}
}

