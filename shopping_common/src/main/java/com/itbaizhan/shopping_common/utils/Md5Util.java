package com.itbaizhan.shopping_common.utils;

import org.apache.commons.codec.digest.DigestUtils;
/*我们存储在数据库的密码一定不能是明文，必须是密文
* 管理端那边我们用了security来加密，用户端没法用，
* 我们就用md5来加密*/
public class Md5Util {
    public final static String md5key = "BAIZHAN"; // 秘钥

    /**
     * 加密
     * @param text 明文
     * @return 密文
     * 原理就是传入明文和密钥然后加密成密文返回
     */
    public static String encode(String text){
        return DigestUtils.md5Hex(text + md5key);
    }

    /**
     * 验证
     * 原理就是传入明文然后加密成密文，再和数据库中的密文进行比对
     * @param text 明文
     * @param cipher  密文
     * @return true/false
     */
    public static boolean verify(String text, String cipher){
        // 将明文转为密文进行比对
        String md5Text = encode(text);
        if (md5Text.equalsIgnoreCase(cipher)) {
            return true;
        }
        return false;
    }
}
