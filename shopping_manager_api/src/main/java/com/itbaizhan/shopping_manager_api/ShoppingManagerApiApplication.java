package com.itbaizhan.shopping_manager_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
//正常项目没有数据库会无法启动，但是这个模块不需要数据库，所以排除掉，让项目能正常启动
@EnableDiscoveryClient //向注册中心注册该服务
@RefreshScope // 配置动态刷新
public class ShoppingManagerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingManagerApiApplication.class, args);
    }

}
