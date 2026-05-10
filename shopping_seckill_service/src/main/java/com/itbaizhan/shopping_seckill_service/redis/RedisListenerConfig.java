package com.itbaizhan.shopping_seckill_service.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 配置redis监听器
 */
//标注一个配置类，语义上表示这个类包含一个或多个 @Bean 方法，用于定义/产生其他 Bean。
@Configuration
public class RedisListenerConfig {
    // 配置redis监听器，监听redis过期事件
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}

