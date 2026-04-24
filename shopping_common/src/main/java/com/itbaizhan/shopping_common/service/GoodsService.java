package com.itbaizhan.shopping_common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.shopping_common.pojo.Goods;

/**
 * 商品服务
 */
public interface GoodsService {
    // 新增商品
    void add(Goods goods);
    // 修改商品
    void update(Goods goods);
    // 根据id查询商品详情
    Goods findById(Long id);
    // 上架/下架商品
    // (我们不删除商品，因为它和订单强关联，删除这个商品查询订单会数据不全，
    // 我们逻辑上删除，即下架)
    void putAway(Long id,Boolean isMarketable);
    // 分页查询
    Page<Goods> search(Goods goods, int page, int size);
}
