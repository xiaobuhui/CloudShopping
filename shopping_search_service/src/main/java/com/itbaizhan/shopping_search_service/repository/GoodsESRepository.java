package com.itbaizhan.shopping_search_service.repository;

import com.itbaizhan.shopping_common.pojo.GoodsES;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import javax.swing.*;

/*ElasticsearchRepository：
Spring Data 提供的 ES 官方父接口，封装了所有现成的 CURD 方法
泛型参数 1：GoodsES
→ 对应 ES 里的商品实体类（你项目里的实体，对应 ES 的索引）
泛型参数 2：Long
→ 对应实体类的 主键 ID 类型（和 GoodsES 里的 @Id 类型一致*/
@Repository
public interface GoodsESRepository extends ElasticsearchRepository<GoodsES,Long> {
}
