package com.simatelex.admin.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * @Author Nancy Zhao
 * @Date 2026/06/09 09:49
 * @Version 1.0
 **/
public class EmailUtil {
    // 通用邮箱正则，兼容多级域名 com.hk / com.cn
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._-]+@[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+){1,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * 校验单个邮箱
     */
    public static boolean isSingleEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
        /*String trimEmail = email.trim();
        // 简单规则：必须有 @ 且 @ 前后有内容，必须有 . 且 . 在 @ 之后
        int atIndex = trimEmail.indexOf("@");
        int dotIndex = trimEmail.lastIndexOf(".");
        return atIndex > 0 && dotIndex > atIndex && dotIndex < trimEmail.length() - 1;*/
    }

    /**
     * 校验多邮箱（英文逗号分隔）
     * @param emailStr 邮箱字符串
     * @return true=全部合法
     */
    public static boolean checkMultiEmail(String emailStr) {
        // 空字符串直接放行（抄送、密送选填）
        if (!StringUtils.hasText(emailStr)) {
            return true;
        }
        String[] emailArr = emailStr.split(";");
        for (String e : emailArr) {
            String val = e.trim();
            // 存在空串 或 格式非法，直接返回不合法
            if (!StringUtils.hasText(val) || !isSingleEmail(val)) {
                return false;
            }
        }
        return true;
    }
}
