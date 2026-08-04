package com.simatelex.admin.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Author Nancy Zhao
 * @Date 2026/06/09 09:26
 * @Version 1.0
 **/
public class NumberUtil {

    public static Boolean isEmpty(BigDecimal number){
        if (number == null || number.compareTo(BigDecimal.ZERO) == 0 || number.equals("")){
            return true;
        }
        return false;
    }

    public static Boolean isEmpty(Integer number){
        if (number == null || number.compareTo(0) == 0 || number.equals("")){
            return true;
        }
        return false;
    }

    /**
     * 去除BigDecimal末尾多余的0，去掉无用小数点
     * 示例：
     * 5000.000 → 5000
     * 0.000 → 0
     * 123.4500 → 123.45
     * @param num 原数值
     * @return 格式化后的BigDecimal
     */
    public static BigDecimal stripTrailingZeros(BigDecimal num) {
        if (num == null) {
            return BigDecimal.ZERO;
        }
        // 去除末尾多余0
        BigDecimal stripped = num.stripTrailingZeros();
        // 如果是整数，转为不带小数点形式
        if (stripped.scale() <= 0) {
            return stripped.setScale(0, RoundingMode.HALF_UP);
        }
        return stripped;
    }

    /**
     * 转字符串直接去掉末尾无效小数点（前端展示用）
     */
    public static String toPlainStringNoTrailingZero(BigDecimal num) {
        return stripTrailingZeros(num).toPlainString();
    }
}
