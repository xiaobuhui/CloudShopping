package com.itbaizhan.shopping_search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.shopping_common.pojo.*;
import com.itbaizhan.shopping_common.service.SearchService;
import com.itbaizhan.shopping_search_service.repository.GoodsESRepository;
import lombok.SneakyThrows;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@DubboService
@Service
public class SearchServiceImpl implements SearchService {
    //Elasticsearch 官方原生新Java 客户端
    @Autowired
    private ElasticsearchClient client;
    //Spring Data 提供的极简 CRUD 接口
    @Autowired
    private GoodsESRepository goodsESRepository;
    //Spring 封装的模板工具类，里面有各种es的操作方法
    @Autowired
    private ElasticsearchTemplate template;
    /**
     * 分词
     * @param text   被分词的文本
     * @param analyzer 分词器
     * @return 分词结果
     */
    @SneakyThrows // 抛出已检查异常
    //Lombok 提供的自动抛异常注解
    public List<String> analyze(String text, String analyzer) {
        /*AnalyzeRequest request = AnalyzeRequest.of(a ->
                a.index("goods")        // 指定ES索引：商品索引
                 .analyzer(analyzer)    // 指定使用的分词器
                 .text(text)            // 要分词的文本
        );*/
        // 分词请求
        AnalyzeRequest request = AnalyzeRequest.of(a ->
                a.index("goods").analyzer(analyzer).text(text)
        );

        /*client：ES 客户端连接对象
        client.indices().analyze(request)：调用 ES 分词接口
        AnalyzeResponse：ES 返回的分词响应结果*/
        // 发送分词请求，获取相应结果
        AnalyzeResponse response = client.indices().analyze(request);

        /*response.tokens()：ES 把文本拆成的一个个分词单元
        AnalyzeToken：每个分词的详细信息（词、位置、长度等）*/
        // 处理相应结果
        List<String> words = new ArrayList(); //存放分词结果集合
        List<AnalyzeToken> tokens = response.tokens();//获取分词令牌列表
        for (AnalyzeToken token : tokens) {
            String term = token.token();// 拿到拆分后的词语
            words.add(term);
        }
        return words;
    }

    // 自动补齐,联想提示
    @Override
    @SneakyThrows
    public List<String> autoSuggest(String keyword) {
        /*/Suggester：ES 官方提供的自动补全专用查询对象，不是普通搜索
        "prefix_suggestion",  // 自定义补全查询名称
        completion：对应你实体类 tags 字段的 @CompletionField 注解
        只有被这个注解标记的字段，才能用自动补全
        field("tags")：指定用 tags 关键字字段做匹配
        参数说明
        skipDuplicates(true)：重复的提示词只保留一个，开启去重
        size(10)：最多返回 10 条联想结果
        text(keyword)：把用户输入的内容作为匹配前缀*/
        // 1.自动补齐查询条件
        Suggester suggester = Suggester.of(
                s -> s.suggesters("prefix_suggestion", FieldSuggester.of(
                        fs -> fs.completion(
                                cs -> cs.skipDuplicates(true)
                                        .size(10)
                                        .field("tags")
                        )
                )).text(keyword)
        );

        // 2.自动补齐查询
        /*client：你之前注入的 ElasticsearchClient 官方原生客户端
        index("goods")：查询 ES 的 goods 索引（和分词、同步数据用的同一个索引）
        suggest(suggester)：执行自动补全查询（不是普通的query查询）
        response：ES 返回的自动补全原始结果*/
        SearchResponse<Map> response = client.search(s -> s.index("goods")
                .suggest(suggester), Map.class);

        // 3.处理查询结果
        /*response.suggest()：提取补全结果集
        get("prefix_suggestion")：对应我们自定义的查询名称，拿到补全数据
        suggestion.completion().options()：提取自动补全的候选词列表
         这就是 ES 匹配到的所有联想词*/
        Map resultMap = response.suggest();
        List<Suggestion> suggestionList = (List) resultMap.get("prefix_suggestion");
        Suggestion suggestion = suggestionList.get(0);
        List<CompletionSuggestOption> resultList = suggestion.completion().options();

        //封装结果并返回前端
        List<String> result = new ArrayList<>();
        for (CompletionSuggestOption completionSuggestOption : resultList) {
            String text = completionSuggestOption.text();
            result.add(text);
        }
        return result;
    }

    // 搜索产品
    @Override
    public GoodsSearchResult search(GoodsSearchParam goodsSearchParam) {
        // 1.构造ES搜索条件，调用我们写的的buildQuery方法，把前端的参数，
        // 转换成 ES 能识别的查询语句（关键词 + 品牌 + 价格 + 规格 + 分页 + 排序）
        //NativeQuery：ES 官方提供的原生查询对象
        NativeQuery query = buildQuery(goodsSearchParam);
        // 2.搜索，SearchHits<GoodsES>：ES 返回的搜索结果集合（包含商品数据、总条数、高亮等）
        SearchHits<GoodsES> search = template.search(query, GoodsES.class);
        // 3.将查询结果封装为Page对象
        // 3.1 将SearchHits转为List
        List<GoodsES> content = new ArrayList();
        for (SearchHit<GoodsES> goodsESSearchHit : search) {
            //用 .getContent() 提取纯商品数据
            GoodsES goodsES = goodsESSearchHit.getContent();
            content.add(goodsES);
        }
        // 3.2 将List转为MP的Page对象
        Page<GoodsES> page = new Page();
        page.setCurrent(goodsSearchParam.getPage()) // 当前页
                .setSize(goodsSearchParam.getSize()) // 每页条数
                .setTotal(search.getTotalHits()) // 总条数
                .setRecords(content); // 结果集

        // 4.封装结果对象
        // 4.1 查询结果，GoodsSearchResult：最终返回给前端的大对象
        GoodsSearchResult result = new GoodsSearchResult();
        result.setGoodsPage(page);
        // 4.2 查询参数，setGoodsSearchParam：把查询条件传回去（前端回显用）
        result.setGoodsSearchParam(goodsSearchParam);
        // 4.3 查询面板，buildSearchPanel：构建筛选面板（品牌、规格、价格）
        buildSearchPanel(goodsSearchParam,result);
        return result;
    }

    /**
     * 封装查询面板，封装查询面板，即根据查询条件，找到查询结果关联度前20名的商品进行封装
     * @param goodsSearchParam
     * @param goodsSearchResult
     */
    public void buildSearchPanel(GoodsSearchParam goodsSearchParam,GoodsSearchResult goodsSearchResult){
        // 1.构造搜索条件
        goodsSearchParam.setPage(1);
        goodsSearchParam.setSize(20);
        goodsSearchParam.setSort(null);
        goodsSearchParam.setSortFiled(null);
        NativeQuery query = buildQuery(goodsSearchParam);
        // 2.搜索
        SearchHits<GoodsES> search = template.search(query, GoodsES.class);
        // 3.将结果封装为List对象
        List<GoodsES> content = new ArrayList();
        for (SearchHit<GoodsES> goodsESSearchHit : search) {
            GoodsES goodsES = goodsESSearchHit.getContent();
            content.add(goodsES);
        }
        // 4.遍历集合，封装查询面板
        // 商品相关的品牌列表
        Set<String> brands = new HashSet();
        // 商品相关的类型列表
        Set<String> productTypes = new HashSet();
        // 商品相关的规格列表
        Map<String, Set<String>> specifications = new HashMap();
        for (GoodsES goodsES : content) {
            // 获取品牌
            brands.add(goodsES.getBrand());
            // 获取类型
            List<String> productType = goodsES.getProductType();
            productTypes.addAll(productType);
            // 获取规格
            Map<String, List<String>> specification = goodsES.getSpecification();
            Set<Map.Entry<String, List<String>>> entries = specification.entrySet();
            for (Map.Entry<String, List<String>> entry : entries) {
                // 规格名
                String key = entry.getKey();
                // 规格值
                List<String> value = entry.getValue();
                // 如果没有遍历出该规格，新增键值对，如果已经遍历出该规格，则向规格中添加规格项
                if (!specifications.containsKey(key)){
                    specifications.put(key,new HashSet(value));
                }else{
                    specifications.get(key).addAll(value);
                }
            }
        }
        goodsSearchResult.setBrands(brands);
        goodsSearchResult.setProductType(productTypes);
        goodsSearchResult.setSpecifications(specifications);
    }


    /**
     * 构造搜索条件
     * @param goodsSearchParam 查询条件对象
     * @return 搜索条件对象
     * 电商商品搜索的核心大脑，专门用来把前端传的各种搜索条件，
     * 拼接成 ES 能识别的复杂查询语句，实现关键词搜索 + 品牌筛选 + 价格区间 +
     * 规格筛选 + 分页 + 排序全能搜索！
     */
    /*接收前端传来的商品搜索参数（关键词、品牌、价格、规格、分页、排序），构建一个ES
    复杂布尔查询，最终返回可直接执行的查询对象，实现精准、全能的商品搜索。
    BoolQuery：ES 布尔查询，must 代表必须满足（等价于 SQL 的 AND）
    GoodsSearchParam：前端传过来的所有搜索条件封装类
    这段代码只拼查询条件，不执行查询，拼好后交给客户端去 ES 搜索
    xxx._toQuery()：把构建好的查询条件，转换成 ES 能识别的标准 Query 对象
    builder.build()：把所有 must 条件拼成完整布尔查询
    withQuery()：把所有查询条件，装进最终的查询构建器
    of(...)ES 查询专用,Java 8+ 静态方法 + Lambda 链式写法*/
    public NativeQuery buildQuery(GoodsSearchParam goodsSearchParam){
        // 1.创建复杂查询条件对象,NativeQueryBuilder：ES原生查询构建器
        NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder();
        //BoolQuery.Builder：布尔查询构建器（所有筛选条件都用它拼接，must = 必须匹配）
        BoolQuery.Builder builder = new BoolQuery.Builder();
        // 2.如果查询条件有关键词，关键词可以匹配商品名、副标题、品牌字段；否则查询所有商品
        //MatchAllQuery查询所有文档（es的数据），这里文档指商品
        if (!StringUtils.hasText(goodsSearchParam.getKeyword())) {
            MatchAllQuery matchAllQuery = new MatchAllQuery.Builder().build();
            builder.must(matchAllQuery._toQuery());
        } else {
            String keyword = goodsSearchParam.getKeyword();
            //MultiMatchQuery：多字段匹配查询，一个关键词可以匹配商品名、副标题、品牌等多个字段
            MultiMatchQuery keywordQuery = MultiMatchQuery.of(q -> q.query(keyword).fields("goodsName", "caption", "brand"));
            builder.must(keywordQuery._toQuery());
        }

        // 3.如果查询条件有品牌，则精准匹配品牌
        // TermQuery：精准匹配（不分词，完全相等）
        String brand = goodsSearchParam.getBrand();
        if (StringUtils.hasText(brand)) {
            TermQuery brandQuery = TermQuery.of(q -> q.field("brand").value(brand));
            builder.must(brandQuery._toQuery());
        }

        // 4.如果查询条件有价格，则匹配价格
        // RangeQuery：范围查询，gte → ≥ 最低价，lte → ≤ 最高价
        Double highPrice = goodsSearchParam.getHighPrice();
        Double lowPrice = goodsSearchParam.getLowPrice();
        if (highPrice != null && highPrice != 0) {
            RangeQuery lte = RangeQuery.of(q -> q.field("price").lte(JsonData.of(highPrice)));
            builder.must(lte._toQuery());
        }
        if (lowPrice != null && lowPrice != 0) {
            RangeQuery gte = RangeQuery.of(q -> q.field("price").gte(JsonData.of(lowPrice)));
            builder.must(gte._toQuery());
        }

        // 5.如果查询条件有规格项，则精准匹配规格项
        Map<String, String> specificationOptions = goodsSearchParam.getSpecificationOption();
        if (specificationOptions != null && specificationOptions.size() > 0) {
            //entrySet()返回Map集合的所有键值对
            Set<Map.Entry<String, String>> entries = specificationOptions.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (StringUtils.hasText(key)) {
                    TermQuery termQuery = TermQuery.of(q -> q.field("specification." + key + ".keyword").value(value));
                    builder.must(termQuery._toQuery());
                }
            }
        }
        nativeQueryBuilder.withQuery(builder.build()._toQuery());

        // 6.添加分页条件，ES 分页页码从 0 开始，前端传page=1，后台要-1
        PageRequest pageable = PageRequest.of(goodsSearchParam.getPage() - 1, goodsSearchParam.getSize());
        //withPageable()：把分页条件绑定到查询对象
        nativeQueryBuilder.withPageable(pageable);

        // 7.如果查询条件有排序，则添加排序条件
        String sortFiled = goodsSearchParam.getSortFiled();
        String sort = goodsSearchParam.getSort();
        if (StringUtils.hasText(sort) && StringUtils.hasText(sortFiled)) {
            Sort sortParam = null;
            // 新品的正序是id的倒序
            if (sortFiled.equals("NEW")) {
                if (sort.equals("ASC")) {
                    sortParam = Sort.by(Sort.Direction.DESC, "id");
                }
                if (sort.equals("DESC")) {
                    sortParam = Sort.by(Sort.Direction.ASC, "id");
                }
            }
            if (sortFiled.equals("PRICE")) {
                if (sort.equals("ASC")) {
                    sortParam = Sort.by(Sort.Direction.ASC, "price");
                }
                if (sort.equals("DESC")) {
                    sortParam = Sort.by(Sort.Direction.DESC, "price");
                }
            }
            //withSort(sortParam)作用：把排序条件绑定到查询对象
            nativeQueryBuilder.withSort(sortParam);
        }
        // 8.返回查询条件对象
        return nativeQueryBuilder.build();
    }

    //向ES同步数据库中的商品数据
    @Override
    public void syncGoodsToES(GoodsDesc goodsDesc) {
        // 将商品详情对象转为GoodsES对象
        GoodsES goodsES = new GoodsES();
        goodsES.setId(goodsDesc.getId());
        goodsES.setGoodsName(goodsDesc.getGoodsName());
        goodsES.setCaption(goodsDesc.getCaption());
        goodsES.setPrice(goodsDesc.getPrice());
        goodsES.setHeaderPic(goodsDesc.getHeaderPic());
        goodsES.setBrand(goodsDesc.getBrand().getName());
        // 类型集合
        List<String> productType = new ArrayList();
        productType.add(goodsDesc.getProductType1().getName());
        productType.add(goodsDesc.getProductType2().getName());
        productType.add(goodsDesc.getProductType3().getName());
        goodsES.setProductType(productType);
        // 规格集合
        Map<String,List<String>> map = new HashMap();
        List<Specification> specifications = goodsDesc.getSpecifications();
        // 遍历规格
        for (Specification specification : specifications) {
            // 规格项集合
            List<SpecificationOption> options = specification.getSpecificationOptions();
            // 规格项名集合
            List<String> optionStrList = new ArrayList();
            for (SpecificationOption option : options) {
                optionStrList.add(option.getOptionName());
            }
            map.put(specification.getSpecName(),optionStrList);
        }
        goodsES.setSpecification(map);
        // 关键字
        List<String> tags = new ArrayList();
        tags.add(goodsDesc.getBrand().getName()); //品牌名是关键字
        tags.addAll(analyze(goodsDesc.getGoodsName(),"ik_smart"));//商品名分词后为关键词
        tags.addAll(analyze(goodsDesc.getCaption(),"ik_smart"));//副标题分词后为关键词
        goodsES.setTags(tags);

        // 将GoodsES对象存入ES
        goodsESRepository.save(goodsES);
    }

    @Override
    public void delete(Long id) {

    }
}
