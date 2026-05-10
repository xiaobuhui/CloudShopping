package com.itbaizhan.shopping_custcare_service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.shopping_common.pojo.Faq;
import com.itbaizhan.shopping_common.service.FaqService;
import com.itbaizhan.shopping_custcare_service.mapper.FaqMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * FAQ服务实现类
 */
@DubboService
@Service
public class FaqServiceImpl implements FaqService {
    @Autowired
    private FaqMapper faqMapper;
    // Spring AI提供的操作向量数据库的工具
    @Autowired
    private VectorStore vectorStore;
    @Value("${spring.ai.vectorstore.qdrant.similarity-threshold}")
    private double similarityThreshold;

    /**
     * 分页查询FAQ答案
     * @param page    页码
     * @param size    每页大小
     * @param categoryId 分类ID
     * @return FAQ答案分页结果
     */
    @Override
    public Page<Faq> getFaqPage(int page, int size, Integer categoryId) {
        QueryWrapper<Faq> wrapper = new QueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq("categoryId", categoryId);
        }
        return faqMapper.selectPage(new Page(page, size), wrapper);
    }

    /**
     * 根据ID查询FAQ答案
     * @param id FAQ答案ID
     * @return FAQ答案实体
     */
    @Override
    public Faq getFaqById(String id) {
        return faqMapper.selectById(id);
    }

    /**
     * 创建FAQ答案
     * @param faq FAQ答案实体
     * @return 创建的FAQ答案ID
     */
    @Override
    public String createFaq(Faq faq) {
        // 设置默认值
        if (faq.getId() == null) {
            faq.setId(UUID.randomUUID().toString());
        }
        if (faq.getStatus() == null) {
            faq.setStatus(1);
        }
        if (faq.getUseCount() == null) {
            faq.setUseCount(0);
        }
        faqMapper.insert(faq);
        // 同步到向量数据库
        if (faq.getStatus() == 1) {
            insertFaqToQdrant(faq);
        }
        return faq.getId();
    }

    /**
     * 更新FAQ答案
     * @param faq FAQ答案实体
     */
    @Override
    public void updateFaq(Faq faq) {
        faqMapper.updateById(faq);
        // 同步数据到向量数据库
        if (faq.getStatus() != null && faq.getStatus() == 0) {
            // 删除旧文档
            deleteFaqVector(faq.getId());
        }else {
            // 删除旧文档，插入新文档
            deleteFaqVector(faq.getId());
            insertFaqToQdrant(faq);
        }
    }


    /**
     * 删除FAQ答案
     * @param id FAQ答案ID
     */
    @Override
    public void deleteFaq(String id) {
        faqMapper.deleteById(id);
        deleteFaqVector(id);
    }

    /**
     * 智能匹配最佳答案
     * @param question 用户问题
     * @return 匹配的FAQ答案
     */
    @Override
    public Faq findBestAnswer(String question) {
        // 向量搜索FAQ答案
        Faq vectorResult = findByVectorSearch(question);
        // 增加FAQ使用次数
        if (vectorResult != null) {
            incrementUseCount(vectorResult.getId());
        }
        return vectorResult;
    }

    /**
     * 使用向量搜索匹配FAQ答案
     */
    private Faq findByVectorSearch(String question) {
        // 构建搜索请求，根究相似度找到一个满足相似度的回答
        SearchRequest searchRequest = SearchRequest
                .query(question)
                .withTopK(1)
                .withSimilarityThreshold(similarityThreshold);
        // 执行搜索
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        if (!results.isEmpty()) {
            Document result = results.get(0);
            String faqId = result.getId().toString();
            return faqMapper.selectById(faqId);
        }
        return null;
    }
    /**
     * 增加FAQ使用次数
     * @param id FAQ答案ID
     */
    private void incrementUseCount(String id) {
        Faq faq = faqMapper.selectById(id);
        faq.setUseCount(faq.getUseCount() + 1);
        faqMapper.updateById(faq);
    }
    /**
     * 从Qdrant删除FAQ向量
     */
    private void deleteFaqVector(String faqId) {
        List<String> docs = new ArrayList();
        docs.add(faqId);
        vectorStore.delete(docs);
    }

    /**
     * 将FAQ插入到Qdrant向量数据库
     */
    private void insertFaqToQdrant(Faq faq) {
        // 构建元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("faq_id", faq.getId());
        metadata.put("category_id", faq.getCategoryId());
        metadata.put("question", faq.getQuestion());
        metadata.put("answer", faq.getAnswer());
        // 构建Document
        Document document = new Document(faq.getId().toString(), faq.getQuestion(), metadata);
        List<Document> docs = new ArrayList();
        docs.add(document);
        vectorStore.add(docs);
    }
    /**
     * 同步FAQ数据到向量数据库
     */
    public void syncToQdrant() {
        QueryWrapper<Faq> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        List<Faq> faqs = faqMapper.selectList(wrapper);
        for (Faq faq : faqs) {
            insertFaqToQdrant(faq);
        }
    }

}

