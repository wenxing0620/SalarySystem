package com.salarysystem.util;

public class DesensitizeUtil {
    // 手机号脱敏：保留前3后4
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) return phone;
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    // 身份证脱敏：保留前3后4
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 15) return idCard;
        return idCard.replaceAll("(\\d{3})\\d+([\\dXx]{4})", "$1***********$2");
    }

    // 姓名脱敏：只显示第一个字和最后一个字
    public static String maskName(String name) {
        if (name == null || name.length() <= 1) return name;
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "**" + name.charAt(name.length() - 1);
    }

    // 住址脱敏：只显示前6个字
    public static String maskAddress(String address) {
        if (address == null || address.length() <= 6) return address;
        return address.substring(0, 6) + "***";
    }
}
