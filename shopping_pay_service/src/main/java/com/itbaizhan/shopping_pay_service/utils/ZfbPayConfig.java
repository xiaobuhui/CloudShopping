package com.itbaizhan.shopping_pay_service.utils;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置,主要读取配置然后往spring容器设置一个支付宝客户端
 * 让我们项目可以连接支付宝服务器进行支付
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class ZfbPayConfig {
    // 应用id
    private String appId;
    // 应用私钥
    private String privateKey;
    // 支付宝公钥
    private String publicKey;
    // 网关
    private String gateway;
    // 回调网址
    private String notifyUrl;
    // 支付成功回调接口
    private String pcNotify;

    /**
     * 设置支付宝客户端
     */
    @Bean
    public AlipayClient setAlipayClient() {
        return new DefaultAlipayClient(gateway, appId, privateKey, "json", "UTF-8", publicKey, "RSA2");
    }
}
