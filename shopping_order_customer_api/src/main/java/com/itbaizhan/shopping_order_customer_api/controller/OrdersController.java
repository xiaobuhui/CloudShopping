package com.itbaizhan.shopping_order_customer_api.controller;

import com.itbaizhan.shopping_common.pojo.CartGoods;
import com.itbaizhan.shopping_common.pojo.Orders;
import com.itbaizhan.shopping_common.result.BaseResult;
import com.itbaizhan.shopping_common.service.CartService;
import com.itbaizhan.shopping_common.service.OrdersService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/orders")
public class OrdersController {
    @DubboReference
    private OrdersService ordersService;
    @DubboReference
    private CartService cartService;

    /**
      * 生成订单
      * @param orders 订单对象
      * @param userId 用户Id
      * @return 生成的订单
      */
    @PostMapping("/add")
    public BaseResult<Orders> add(@RequestBody Orders orders, @RequestHeader Long userId){
        orders.setUserId(userId);
        // 保存订单
        Orders orders1 = ordersService.add(orders);
        // 将redis中购物车商品删除
        List<CartGoods> cartGoods = orders.getCartGoods();
        for (CartGoods cartGood : cartGoods) {
            cartService.deleteCartOption(userId,cartGood.getGoodId());
        }
        return BaseResult.ok(orders1);
    }
    /**
     * 根据id查询订单
     * @param id 订单id
     * @return 查询结果
     */
    @GetMapping("/findById")
    public BaseResult<Orders> findById(String id){
        Orders orders = ordersService.findById(id);
        return BaseResult.ok(orders);
    }

    /**
     * 查询用户的订单
     * @param status 订单状态：1.未付款 2.已付款 3.
    未发货 4.已发货 5.交易成功 6.交易关闭 7.待评价，传入空
    值代表查询所有
     * @param userId 用户id
     * @return 查询结果
     */
    @GetMapping("/findUserOrders")
    public BaseResult<List<Orders>>
    findUserOrders(Integer status,@RequestHeader
    Long userId){
        List<Orders> orders = ordersService.findUserOrders(userId, status);
        return BaseResult.ok(orders);
    }
}
