package com.itbaizhan.shopping_manager_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class ShoppingManagerApiApplicationTests {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Test
    void contextLoads() {
        //对密码进行加密
        String encode = passwordEncoder.encode("sxt");
        System.out.println(encode);
    }

}
