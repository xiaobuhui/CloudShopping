package com.itbaizhan.shopping_search_customer_api.controller;

import com.itbaizhan.shopping_common.pojo.GoodsDesc;
import com.itbaizhan.shopping_common.pojo.GoodsSearchParam;
import com.itbaizhan.shopping_common.pojo.GoodsSearchResult;
import com.itbaizhan.shopping_common.result.BaseResult;
import com.itbaizhan.shopping_common.service.GoodsService;
import com.itbaizhan.shopping_common.service.SearchService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户商品搜索
 */
@RestController
@RequestMapping("/user/goodsSearch")
public class GoodsSearchController {
    @DubboReference
    private SearchService searchService;
    @DubboReference
    private GoodsService goodsService;

    /**
     * 自动补齐关键字
     * @param keyword 被补齐的词
     * @return 补齐的关联词集合
     */
    @GetMapping("/autoSuggest")
    public BaseResult<List<String>> autoSuggest(String keyword){
        List<String> keywords = searchService.autoSuggest(keyword);
        return BaseResult.ok(keywords);
    }

    /**
     * 搜索商品
     * @param goodsSearchParam 搜索条件
     * @return 搜索结果
     */
    @PostMapping("/search")
    public BaseResult<GoodsSearchResult> search(@RequestBody GoodsSearchParam goodsSearchParam){
        GoodsSearchResult result = searchService.search(goodsSearchParam);
        return BaseResult.ok(result);
    }
    /**
     * 根据id查询商品详情
     * @param id 商品id
     * @return 商品详情
     */
    @GetMapping("/findDesc")
    public BaseResult<GoodsDesc> findDesc(Long id) {
        GoodsDesc goodsDesc = goodsService.findDesc(id);
        return BaseResult.ok(goodsDesc);
    }

}
