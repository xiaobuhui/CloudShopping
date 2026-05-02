package com.itbaizhan.shopping_cart_service.listener;

import com.itbaizhan.shopping_common.pojo.CartGoods;
import com.itbaizhan.shopping_common.service.CartService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// 监听同步商品消息
@Service
@RocketMQMessageListener(topic = "sync_cart_queue",
        consumerGroup = "sync_cart_group")
public class SyncCartListener implements RocketMQListener<CartGoods> {
    @Autowired
    private CartService cartService;
    @Override
    public void onMessage(CartGoods cartGoods) {
        System.out.println("同步购物车商品");
        cartService.refreshCartGoods(cartGoods);
    }
}




