package com.salarysystem.util;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

/**
 * SM 系列加解密与 Hash 工具
 */
public class SmCryptoUtil {

    /**
     * SM4 密钥 (示例硬编码)
     * 注意：生产环境请从安全配置中加载密钥，切勿硬编码
     */
    private static final String SM4_KEY = "1234567890abcdef";

    // 默认 HMAC 密钥（示例用）
    private static final String DEFAULT_HMAC_KEY = "change_this_hmac_key_in_production";

    // 初始化 SM4 实例
    private static final SymmetricCrypto SM4 = SmUtil.sm4(SM4_KEY.getBytes());

    /**
     * SM3 散列（不可逆）
     */
    public static String hashSm3(String plainText) {
        if (plainText == null) return null;
        return SmUtil.sm3(plainText);
    }

    /**
     * SM4 加密（明文 -> 密文 hex）
     */
    public static String encryptSm4(String plainText) {
        if (plainText == null) return null;
        return SM4.encryptHex(plainText);
    }

    /**
     * SM4 解密（密文 hex -> 明文）
     */
    public static String decryptSm4(String cipherText) {
        if (cipherText == null) return null;
        return SM4.decryptStr(cipherText);
    }

    /**
     * 计算 HMAC-SM3 并返回 hex 字符串
     * 使用 BouncyCastle 的 HMac 与 SM3 实现（正确的 HMAC 构造）
     */
    public static String hmacSm3Hex(String key, String message) {
        if (key == null) key = DEFAULT_HMAC_KEY;
        if (message == null) message = "";
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        HMac hmac = new HMac(new SM3Digest());
        hmac.init(new KeyParameter(keyBytes));
        hmac.update(msgBytes, 0, msgBytes.length);
        byte[] out = new byte[hmac.getMacSize()];
        hmac.doFinal(out, 0);
        return Hex.toHexString(out);
    }

    /**
     * 使用默认 HMAC 密钥计算 HMAC-SM3
     */
    public static String hmacSm3Hex(String message) {
        return hmacSm3Hex(DEFAULT_HMAC_KEY, message);
    }

    // 简单测试
    public static void main(String[] args) {
        System.out.println("密码 'Admin@123' 的SM3值: " + hashSm3("Admin@123"));

        String idCard = "110105199001011234";
        String encrypted = encryptSm4(idCard);
        System.out.println("身份证加密后存入DB: " + encrypted);
        System.out.println("从DB读取后解密展示: " + decryptSm4(encrypted));

        String h = hmacSm3Hex("mykey", "user|LOGIN|127.0.0.1|2026-01-01T12:00:00");
        System.out.println("HMAC-SM3: " + h);
    }
}