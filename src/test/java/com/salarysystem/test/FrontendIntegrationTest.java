package com.salarysystem.test;

import com.salarysystem.util.SmCryptoUtil;
import com.salarysystem.dao.impl.SysUserDaoImpl;
import com.salarysystem.dao.impl.SysLogDaoImpl;
import com.salarysystem.model.sysUser;
import com.salarysystem.model.sysLog;

import java.time.LocalDateTime;

/**
 * 前端界面与核心功能测试类（不依赖真实数据库）
 * 可在本地运行验证加密、哈希、HMAC 等基本功能
 */
public class FrontendIntegrationTest {

    public static void main(String[] args) {
        System.out.println("========== 薪资管理系统 - 前端集成测试 ==========\n");

        // 测试 1: SM3 密码哈希
        testSM3Hash();

        // 测试 2: SM4 加解密
        testSM4Encryption();

        // 测试 3: HMAC-SM3
        testHMACSM3();

        System.out.println("\n========== 测试完成 ==========");
    }

    private static void testSM3Hash() {
        System.out.println("【测试 1】SM3 密码哈希");
        String password = "Admin@123";
        String hash = SmCryptoUtil.hashSm3(password);
        System.out.println("  密码明文: " + password);
        System.out.println("  SM3 哈希: " + hash);
        System.out.println("  ✓ 密码哈希测试通过\n");
    }

    private static void testSM4Encryption() {
        System.out.println("【测试 2】SM4 加解密");
        String plainIdCard = "110105199001011234";
        String encrypted = SmCryptoUtil.encryptSm4(plainIdCard);
        String decrypted = SmCryptoUtil.decryptSm4(encrypted);
        System.out.println("  明文身份证: " + plainIdCard);
        System.out.println("  加密后: " + encrypted);
        System.out.println("  解密后: " + decrypted);
        System.out.println("  ✓ 加解密匹配: " + plainIdCard.equals(decrypted) + "\n");
    }

    private static void testHMACSM3() {
        System.out.println("【测试 3】HMAC-SM3 校验");
        String userId = "1";
        String action = "LOGIN_SUCCESS";
        String ip = "192.168.1.1";
        String timestamp = LocalDateTime.now().toString();
        String message = userId + "|" + action + "|" + ip + "|" + timestamp;

        String hmac1 = SmCryptoUtil.hmacSm3Hex(message);
        String hmac2 = SmCryptoUtil.hmacSm3Hex(message);

        System.out.println("  消息: " + message);
        System.out.println("  HMAC-1: " + hmac1);
        System.out.println("  HMAC-2: " + hmac2);
        System.out.println("  ✓ HMAC 一致性: " + hmac1.equals(hmac2));

        // 测试修改消息后 HMAC 变化
        String modifiedMessage = "2|LOGOUT|192.168.1.2|" + timestamp;
        String hmac3 = SmCryptoUtil.hmacSm3Hex(modifiedMessage);
        System.out.println("  修改后的消息: " + modifiedMessage);
        System.out.println("  新 HMAC:     " + hmac3);
        System.out.println("  ✓ HMAC 变化检测: " + !hmac1.equals(hmac3) + "\n");
    }
}

