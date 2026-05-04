package com.itbaizhan.shopping_common.service;

import com.itbaizhan.shopping_common.pojo.Orders;

import java.util.List;

public interface OrdersService {
    // 生成订单
    Orders add(Orders orders);
    //订单数据非常有价值，不能删除
    // 修改订单(订单创建成功后只能修改订单状态，其它的不能修改)
    void update(Orders orders);
    // 根据id查询订单
    Orders findById(String id);
    // 查询用户的订单
    List<Orders> findUserOrders(Long userId, Integer status);
}
