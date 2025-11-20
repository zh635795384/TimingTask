package com.simatelex.admin.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nancy zhao
 * @version 1.0
 */
public class PageResultUtil {
    /**
     * //返回Map给浏览器
     * @param total
     * @param records
     * @return
     */
    public static Map<String,Object> getResult(Long total, List<?> records){
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("count",total);
        result.put("data",records);
        result.put("code",0);
        if (records == null || records.isEmpty()) {
            result.put("msg","No Data");
        } else {
            result.put("msg","");
        }
        return result;
    }

    public static Map<String,Object> getResult(Long total, List<?> records, Map<String, Object> totalRow){
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("count",total);
        result.put("data",records);
        result.put("code",0);
        result.put("msg","");
        result.put("totalRow",totalRow);
        return result;
    }
}
