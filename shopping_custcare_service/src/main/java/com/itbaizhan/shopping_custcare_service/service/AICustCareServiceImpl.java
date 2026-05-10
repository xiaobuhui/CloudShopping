package com.itbaizhan.shopping_custcare_service.service;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.itbaizhan.shopping_common.pojo.Faq;
import com.itbaizhan.shopping_common.service.AICustCareService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * AI客服服务实现类
 */
@DubboService
public class AICustCareServiceImpl implements AICustCareService {
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private FaqServiceImpl faqService;
    @Autowired
    private SensitiveWordBs sensitiveWordBs;
    //注入ai的角色，要求
    @Value("${spring.ai.dashscope.prompt}")
    private String prompt;

    @Override
    public String generateResponse(String userMessage, Long userId) {
        // 1.检查敏感词
        String error = check(userMessage);
        if (error != null) {
            return error;
        }
        // 2.先尝试FAQ回答
        Faq bestAnswer = faqService.findBestAnswer(userMessage);
        if (bestAnswer != null) {
            return bestAnswer.getAnswer();
        }
        // 3.如果没有FAQ答案,调用AI模型回答
        prompt += """
                  你是一个专业的电商客服助手，专门处理订单查询和商品查询请求。请严格按照以下规则执行：
                  1. **意图识别**：
                  - 当用户提及"订单"、"我的订单"、"购买记录"、"待支付"、"已发货"、"最近订单"等关键词时，识别为订单查询意图
                  - 当用户提及"商品"、"查找"、"搜索"、"手机"、"衣服"、"价格"、"库存"等关键词时，识别为商品查询意图
                  2. **服务调用**：
                  - 订单查询意图 → 调用 `getOrderQueryService` 方法
                  - 商品查询意图 → 调用 `getGoodsQueryService` 方法
                  请确保准确识别用户意图并正确调用相应服务，提取所有可用参数填充到函数调用中，用户ID从prompt获取。
                  仅当用户明确要求其他操作或工具无法处理时，才直接回复。
                  当前用户的id: {user_id}
                """;
        // 添加当前时间用户Id
        PromptTemplate promptTemplate = new PromptTemplate(prompt);
        String resolvedPrompt = promptTemplate.render(
                Map.of("current_date", LocalDate.now().toString(),
                        "user_id", userId)
        );
        // AI回答
        String content = chatClient
                .prompt()
                .system(resolvedPrompt)
                .user(userMessage)
                // 记忆管理
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId) // 使用用户ID作为会话ID
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,10)) // 检索最近10条消息
                .call()
                .content();
        return content;
    }


    /**
     * 检查文本是否包含敏感词
     *
     * @param text 文本
     * @return 错误信息，如果没有错误返回null
     */
    private String check(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "内容不能为空";
        }
        if (sensitiveWordBs.contains(text)) {
            return "内容包含敏感词汇，这个问题我暂时无法回答，让我们换个话题再聊聊吧！";
        }
        return null;
    }
}
