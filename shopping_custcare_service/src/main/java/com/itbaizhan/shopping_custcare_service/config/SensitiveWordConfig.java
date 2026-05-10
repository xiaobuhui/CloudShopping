package com.itbaizhan.shopping_custcare_service.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.itbaizhan.shopping_common.pojo.SensitiveWord;
import com.itbaizhan.shopping_custcare_service.mapper.SensitiveWordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SensitiveWordConfig {
    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                // 黑名单：默认+自定义
                .wordDeny(WordDenys.chains(WordDenys.defaults(), new IWordDeny() {
                    @Override
                    public List<String> deny() {
                        QueryWrapper<SensitiveWord> wrapper = new QueryWrapper<>();
                        wrapper.eq("type", "deny");
                        return sensitiveWordMapper.selectList(wrapper)
                                .stream()
                                .map(SensitiveWord::getWord)
                                .toList();
                    }
                }))
                // 白名单：默认+自定义
                .wordAllow(WordAllows.chains(WordAllows.defaults(), new IWordAllow() {
                    @Override
                    public List<String> allow() {
                        QueryWrapper<SensitiveWord> wrapper = new QueryWrapper<>();
                        wrapper.eq("type", "allow");
                        return sensitiveWordMapper.selectList(wrapper)
                                .stream()
                                .map(SensitiveWord::getWord)
                                .toList();
                    }
                }))
                // 忽略大小写
                .ignoreCase(true)
                // 忽略全角/半角
                .ignoreWidth(true)
                // 启用邮箱检查
                .enableEmailCheck(true)
                // 启用URL检查
                .enableUrlCheck(true)
                // 启用数字检查
                .enableNumCheck(true)
                // 初始化
                .init();
    }
}
