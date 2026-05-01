package com.itbaizhan.shopping_user_service.util;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.lang.JoseException;

// 生成公钥和私钥的工具类
// 生成后才能用公钥和私钥来搞jwt验证
/*服务端：用私钥给用户信息签名 → 生成 JWT 令牌
  客户端：拿着令牌访问接口
  验证方：用公钥解密验证 → 确认令牌是真的、没被改*/
public class GenerateJwtDemo {
    public static void main(String[] args) throws JoseException {
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        final String publicKeyString = rsaJsonWebKey.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        final String privateKeyString = rsaJsonWebKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        System.out.println(publicKeyString);
        System.out.println(privateKeyString);
    }
}

