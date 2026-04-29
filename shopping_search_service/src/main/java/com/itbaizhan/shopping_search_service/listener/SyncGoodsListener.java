package com.itbaizhan.shopping_search_service.listener;

import com.itbaizhan.shopping_common.pojo.GoodsDesc;
import com.itbaizhan.shopping_common.service.SearchService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// 监听同步商品消息
@Service
/*topic:监听的Topic名称，必须和生产者发送的Topic一致
* consumerGroup:消费者组名称
*/
@RocketMQMessageListener(topic =
        "sync_goods_queue", consumerGroup =
        "sync_goods_group")
//泛型GoodsDesc表示监听的消息类型为GoodsDesc，当消息过来时会自动转换成GoodsDesc对象
public class SyncGoodsListener implements RocketMQListener<GoodsDesc> {
    @Autowired
    private SearchService searchService;
    /*
       核心方法：收到消息就会自动调用
       参数 GoodsDesc goodsDesc：RocketMQ帮你自动转好的对象
       你不用自己转JSON，直接用！
    */
    @Override
    public void onMessage(GoodsDesc goodsDesc) {
        System.out.println("同步es商品");
        // 调用业务方法，把商品信息同步到 Elasticsearch
        searchService.syncGoodsToES(goodsDesc);
    }
}
