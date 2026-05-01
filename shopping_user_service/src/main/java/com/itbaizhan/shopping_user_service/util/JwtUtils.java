package com.itbaizhan.shopping_user_service.util;

import lombok.SneakyThrows;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {
    // 私钥
    public final static String PRIVATE_JSON = "{\"kty\":\"RSA\",\"n\":\"7qouQUDaK_KM7jyriiQYlTcn7A7N-urdXXqA1ZHP8_UDAmVZjNmlP1xfkoU9vpYPbzsvJsNxERL6JMJvZOTvxS3iYftHADX3Af2_nqjnO73dGxSd9_Av6__ZIzyTTx-w_86jysY5yR7coQBnV7DpVgMS4gFOwRxEn0vcR03bOfmY0yc5uKNAAdX6JxcHzw0-FYNH7yP4J73KcL6w063ekSIoeQ4EnY1DFeIHtBtNxyjIiRCxUCLqpen9bxpo4jhTUbDkzZvF-ziO2Yo_R0z-A5tVQEZ7nIAGLDrcfq-tCN0dPqzUar5cJqGEPAGUVoqmaXJfcrVqi6Viqnd_G0xSaw\",\"e\":\"AQAB\",\"d\":\"Qqx3PJKGAJYDigCX-YEI_xKIDBm8sHbRfa5d9IwGGMSfYRl4MXGSCQfgyTmA3M_mIvBZRU5_NUJkcEWDQh_03dRqNG4y0RvCVCOT1xqfcqChdtHFKcFHWfpBKmdfTdDwLR6IwnqrJQUCpNXMiQJqxTUZntq3qhogD5JtSNw2vNR0IfoyGGToLf8MQTN1CgkkQVkstd_eyWxMp8gXd1PGnyKkGyGhYRtSwNWKqGW4tOaOWYYDm7BZJ6fCtFJh0DEBub949_h_VVJa7dEwk6aGPqk69vC1OsDPTFoMhju8oR_2Z91iaO1xGMk0TZL4S-GMzTcmeuktFcx8IwRBL16AJQ\",\"p\":\"9kh1c4yV9bwmuEROn0WTLAZ_zob162cIca3PTJBCouh3S7l3jfUV8BAwv1FSaejwTttifMlzHVvenEx2tV1WZZORqx0pNxosX8A1ToPTR22T6z54DDtULUPWrzNyHLwViLhugXt3sju3st7UVs33qBpP8yPqU10LBaOYn36LnFc\",\"q\":\"-BTGV2oyi7DoJFgdWyt2Y5kiareymjXcNc-V_ZcmcIHa6PjrhPV8finMY1olKqrIMt6bgGuzclGMGuDytwOBF_BCvrv4OGY-NAEiH06Lc5te4ygdwX_yRfg4w0RfEyhlSgqnhmg8AG5_yWWh4oQYTPxA39hPTYhc93_UEbI4bg0\",\"dp\":\"Yzhv8RxrcfPaWZRTZIFgOHVp2ievPaZl1X6jrIHWBtHG9gADdXKO4wdyzDEUFc0du3dRJ1r1gJd5iNiZXpn667NXFWwLJcLqq6zBDR-45-byl-yz6qGgWRgijrUYPRFdT5aWFp5Ka8j5ShvRyiLyqovnN9p5Vhp_DUeKfn85-cM\",\"dq\":\"E4lHSVECo_-NeHc78JcOnq9Lh43p0b0WF2K-lBbYHxoKzDf2DCVs5V6TSuupa5_BXkgrhMR2gDUtzFwUoRb0jpcMGosg9AgRM_U247JCxxrgMRFSro9N2_a9OZtjxVV0DI2kPbBtsTcCdzC6u-TdKQrgbQBS1m87qv31DGO1j-0\",\"qi\":\"xsf47Eebl1pUqO32IZsfG0Qq1wgTtnCF7PKZkbJS7uaqGyiRkgZzoLAHpsUy9LQUWei8SwepoqIX8uop6rQ6vfc-_kmWdLTiorynTq9fmz9zscGjbjHMuGLmp_CbktYYHDWqrnmPPtoHzxTgmA0qBAwJBtYjQTgPoZDSy3l6wBU\"}";
    // 公钥
    public final static String PUBLIC_JSON = "{\"kty\":\"RSA\",\"n\":\"7qouQUDaK_KM7jyriiQYlTcn7A7N-urdXXqA1ZHP8_UDAmVZjNmlP1xfkoU9vpYPbzsvJsNxERL6JMJvZOTvxS3iYftHADX3Af2_nqjnO73dGxSd9_Av6__ZIzyTTx-w_86jysY5yR7coQBnV7DpVgMS4gFOwRxEn0vcR03bOfmY0yc5uKNAAdX6JxcHzw0-FYNH7yP4J73KcL6w063ekSIoeQ4EnY1DFeIHtBtNxyjIiRCxUCLqpen9bxpo4jhTUbDkzZvF-ziO2Yo_R0z-A5tVQEZ7nIAGLDrcfq-tCN0dPqzUar5cJqGEPAGUVoqmaXJfcrVqi6Viqnd_G0xSaw\",\"e\":\"AQAB\"}";

    /**
     * 生成token
     * @param userId    用户id
     * @param username 用户名字
     * @return
     */
    @SneakyThrows
    public static String sign(Long userId, String username) {
        // 1、 创建jwtclaims  jwt内容载荷部分
        JwtClaims claims = new JwtClaims();
        // 是谁创建了令牌并且签署了它
        claims.setIssuer("itbaizhan");
        // 令牌将被发送给谁
        claims.setAudience("audience");
        // 失效时间长 （分钟）
        claims.setExpirationTimeMinutesInTheFuture(60 * 24);
        // 令牌唯一标识符
        claims.setGeneratedJwtId();
        // 当令牌被发布或者创建现在
        claims.setIssuedAtToNow();
        // 再次之前令牌无效
        claims.setNotBeforeMinutesInThePast(2);
        // 主题
        claims.setSubject("shopping");
        // 可以添加关于这个主题得声明属性
        claims.setClaim("userId", userId);
        claims.setClaim("username", username);

        // 2、签名
        JsonWebSignature jws = new JsonWebSignature();
        //赋值载荷
        jws.setPayload(claims.toJson());

        // 3、jwt使用私钥签署
        PrivateKey privateKey = new RsaJsonWebKey(JsonUtil.parseJson(PRIVATE_JSON)).getPrivateKey();
        jws.setKey(privateKey);

        // 4、设置关键 kid
        jws.setKeyIdHeaderValue("keyId");

        // 5、设置签名算法
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        // 6、生成jwt
        String jwt = jws.getCompactSerialization();
        return jwt;
    }

    /**
     * 解密token，获取token中的信息
     * @param token
     */
    @SneakyThrows
    public static Map<String, Object> verify(String token){
        // 1、引入公钥
        PublicKey publicKey = new RsaJsonWebKey(JsonUtil.parseJson(PUBLIC_JSON)).getPublicKey();
        // 2、使用jwtcoonsumer  验证和处理jwt
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime() //过期时间
                .setAllowedClockSkewInSeconds(30) //允许在验证得时候留有一些余地 计算时钟偏差  秒
                .setRequireSubject() // 主题生命
                .setExpectedIssuer("itbaizhan") // jwt需要知道谁发布得 用来验证发布人
                .setExpectedAudience("audience") //jwt目的是谁 用来验证观众
                .setVerificationKey(publicKey) // 用公钥验证签名  验证密钥
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, AlgorithmIdentifiers.RSA_USING_SHA256))
                .build();
        // 3、验证jwt 并将其处理为 claims
        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
            return jwtClaims.getClaimsMap();
        }catch (Exception e){
            return new HashMap();
        }
    }

    public static void main(String[] args){
        // 生成
        String baizhan = sign(1001L, "baizhan");
        System.out.println(baizhan);

        Map<String, Object> stringObjectMap = verify(baizhan);
        System.out.println(stringObjectMap.get("userId"));
        System.out.println(stringObjectMap.get("username"));
    }
}

