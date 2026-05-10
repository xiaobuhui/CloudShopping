package com.itbaizhan.shopping_seckill_customer_api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.shopping_common.pojo.Orders;
import com.itbaizhan.shopping_common.pojo.SeckillGoods;
import com.itbaizhan.shopping_common.result.BaseResult;
import com.itbaizhan.shopping_common.service.OrdersService;
import com.itbaizhan.shopping_common.service.SeckillService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀商品
 */
@Slf4j
@RestController
@RequestMapping("/user/seckillGoods")
public class SeckillGoodsController {
    @DubboReference
    private SeckillService seckillService;
    @DubboReference
    private OrdersService ordersService;


    /**
     * 用户分页查询秒杀商品
     * @param page 页数
     * @param size 每页条数
     * @return 查询结果
     */
    @GetMapping("/findPage")
    public BaseResult<Page<SeckillGoods>> findPage(int page, int size){
        Page<SeckillGoods> seckillGoodsPage = seckillService.findPageByRedis(page, size);
        return BaseResult.ok(seckillGoodsPage);
    }

    /**
     * 用户查询秒杀商品详情
     * @param id 商品Id
     * @return 查询结果
     */
    @GetMapping("/findById")
    public BaseResult<SeckillGoods> findById(Long id) {
        // 从redis中查询秒杀商品详情
        SeckillGoods seckillGoods = seckillService.findSeckillGoodsByRedis(id);
        if (seckillGoods != null) {
            return BaseResult.ok(seckillGoods);
        } else {
            // 如果redis中查找不到，再从数据库查询秒杀商品详情
            SeckillGoods secillGoodsByMySql = seckillService.findSecillGoodsByMySql(id);
            log.info("执行到这一步了");
            return BaseResult.ok(secillGoodsByMySql);
        }
    }
    /**
     * 生成秒杀订单
     * @param orders 订单对象
     * @return 生成的订单
     */
    @PostMapping("/add")
    public BaseResult<Orders> add(@RequestBody Orders orders, @RequestHeader Long userId){
        orders.setUserId(userId);
        Orders order = seckillService.createOrder(orders);
        return BaseResult.ok(order);
    }

    /**
     * 根据id查询秒杀订单
     * @param id 订单id
     * @return 查询结果
     */
    @GetMapping("/findOrder")
    public BaseResult<Orders> findOrder(String id){
        Orders orders = seckillService.findOrder(id);
        return BaseResult.ok(orders);
    }

    /**
     * 支付秒杀订单
     * @param id 订单id
     */
    @GetMapping("/pay")
    public BaseResult pay(String id){
        // 支付秒杀订单
        Orders orders = seckillService.pay(id);
        // 将订单数据存入数据库
        ordersService.add(orders);
        return BaseResult.ok();
    }

}

