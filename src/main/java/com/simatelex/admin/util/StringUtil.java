package com.simatelex.admin.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Author Nancy Zhao
 * @Date 2026/06/09 09:26
 * @Version 1.0
 **/
public class StringUtil {
    /**
     * 忽略字段名大小写取值
     * @param row 单行数据Map
     * @param fieldName 原始字段名（大小写不限）
     * @return 字段值
     */
    public static String getValueIgnoreCase(Map<String, Object> row, String fieldName) {
        if (row == null || StringUtils.isBlank(fieldName)) {
            return null;
        }
        String targetKey = fieldName.toLowerCase();
        // 遍历所有key，匹配小写相等的key
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey().toLowerCase().equals(targetKey)) {
                Object val = entry.getValue();
                return val == null ? null : String.valueOf(val).trim();
            }
        }
        return null;
    }
}
