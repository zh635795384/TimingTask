package com.simatelex.admin.util;

import java.math.BigDecimal;

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
}
