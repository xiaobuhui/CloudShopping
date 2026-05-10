package com.itbaizhan.shopping_seckill_service.redis;

import com.itbaizhan.shopping_common.pojo.Orders;
import com.itbaizhan.shopping_common.pojo.SeckillGoods;
import com.itbaizhan.shopping_common.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * redis监听类继承KeyExpirationEventMessageListener
 */
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillService seckillService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 重写监听方法
     * 订单过期后，关闭交易，回退商品库存
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 获取失效key，即订单id
        String expiredKey = message.toString();

        // 拿到复制订单信息
        Orders orders = (Orders) redisTemplate.opsForValue().get(expiredKey + "_copy");
        Long goodId = orders.getCartGoods().get(0).getGoodId(); // 产品id
        Integer num = orders.getCartGoods().get(0).getNum(); // 产品数量

        // 查询秒杀商品
        SeckillGoods seckillGoods = seckillService.findSeckillGoodsByRedis(goodId);

        // 回退库存
        seckillGoods.setStockCount(seckillGoods.getStockCount() + num);
        redisTemplate.boundHashOps("seckillGoods").put(goodId, seckillGoods);

        // 删除复制订单数据
        redisTemplate.delete(expiredKey+"_copy");
    }
}
