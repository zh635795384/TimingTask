package com.simatelex.admin.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * @Author Nancy Zhao
 * @Date 2026/06/09 09:49
 * @Version 1.0
 **/
public class EmailUtil {
    // 兼容单段域名（如 simatelex）和多级域名（如 simatelex.com.hk）
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._-]+@[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * 校验单个邮箱
     */
    public static boolean isSingleEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * 校验多邮箱（英文分号分隔）
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
            // 空串（末尾分号导致）或格式非法，返回false
            if (StringUtils.hasText(val) && !isSingleEmail(val)) {
                return false;
            }
        }
        return true;
    }
}
