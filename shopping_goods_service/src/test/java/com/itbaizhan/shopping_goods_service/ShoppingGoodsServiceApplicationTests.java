package com.itbaizhan.shopping_goods_service;

import com.itbaizhan.shopping_common.pojo.GoodsDesc;
import com.itbaizhan.shopping_common.service.GoodsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ShoppingGoodsServiceApplicationTests {
	@Autowired
	private GoodsService goodsService;

	@Test
	void contextLoads() {
		List<GoodsDesc> goodsDescs = goodsService.findAll();
		System.out.println(goodsDescs);
	}

}
