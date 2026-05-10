package com.itbaizhan.shopping_custcare_service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.itbaizhan.shopping_common.pojo.SensitiveWord;
import com.itbaizhan.shopping_common.service.SensitiveWordService;
import com.itbaizhan.shopping_custcare_service.mapper.SensitiveWordMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 敏感词服务实现类
 */
@DubboService
public class SensitiveWordServiceImpl implements SensitiveWordService {
    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    @Autowired
    private SensitiveWordBs sensitiveWordBs; // 敏感词配置类
    /**
     * 添加敏感词
     * @param sensitiveWord 敏感词对象
     */
    @Override
    public void addSensitiveWord(SensitiveWord sensitiveWord) {
        sensitiveWordMapper.insert(sensitiveWord);
        // 刷新配置
        if (sensitiveWord.getType().equals("deny")){
            // 添加到黑名单
            sensitiveWordBs.addWord(sensitiveWord.getWord());
        }else if (sensitiveWord.getType().equals("allow")){
            // 添加到白名单
            sensitiveWordBs.addWordAllow(sensitiveWord.getWord());
        }
    }
    /**
     * 删除敏感词
     * @param id 敏感词ID
     */
    @Override
    public void deleteSensitiveWord(Long id) {
        SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(id);
        sensitiveWordMapper.deleteById(id);
        // 刷新配置
        if (sensitiveWord.getType().equals("deny")){
            // 删除黑名单
            sensitiveWordBs.removeWord(sensitiveWord.getWord());
        }else if (sensitiveWord.getType().equals("allow")){
            // 删除白名单
            sensitiveWordBs.removeWordAllow(sensitiveWord.getWord());
        }
    }
    /**
     * 分页查询敏感词
     * @param page 页码
     * @param size 每页大小
     * @param word 关键词（可选，模糊匹配）
     * @param type 敏感词类型 ("deny" 或 "allow")
     * @return
     */
    @Override
    public Page<SensitiveWord> findSensitiveWords(int page, int size, String word , String type) {
        QueryWrapper<SensitiveWord> wrapper = new QueryWrapper<>();
        if (word != null && !word.isEmpty()){
            wrapper.like("word", word);
        }
        if (type != null && !type.isEmpty()){
            wrapper.eq("type", type);
        }
        return sensitiveWordMapper.selectPage(new Page(page, size), wrapper);
    }
}

