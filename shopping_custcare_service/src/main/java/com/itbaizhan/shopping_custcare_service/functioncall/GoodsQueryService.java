package com.itbaizhan.shopping_custcare_service.functioncall;

import com.itbaizhan.shopping_common.pojo.GoodsES;
import com.itbaizhan.shopping_common.pojo.GoodsSearchParam;
import com.itbaizhan.shopping_common.service.SearchService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class GoodsQueryService implements Function<GoodsQueryService.Request,GoodsQueryService.Response> {
    @DubboReference
    private SearchService searchService;
    /**
     * 应用
     * @param
     * @return
     */
    @Override
    public GoodsQueryService.Response apply(Request request) {
        Integer size = request.size == null ? 1 : request.size; // 默认 1 条
        String keyword = request.keyword == null ? "" : request.keyword; // 默认空
        GoodsSearchParam goodsSearchParam = new GoodsSearchParam();
        goodsSearchParam.setPage(1);
        goodsSearchParam.setSize(size);
        goodsSearchParam.setKeyword(keyword);
        List<GoodsES> goodsESList = searchService.search(goodsSearchParam).getGoodsPage().getRecords();
        return new Response(goodsESList);
    }
    /**
     * 输入
     * @param keyword 关键字
     * @param size 查询条数
     */
    public record Request(String keyword,Integer size){}
    /**
     * 输出
     * @param goodsESList 商品信息集合
     */
    public record Response(List<GoodsES> goodsESList){}
}

