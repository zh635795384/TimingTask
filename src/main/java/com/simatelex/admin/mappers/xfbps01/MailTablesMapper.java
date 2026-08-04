package com.simatelex.admin.mappers.xfbps01;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Nancy Zhao
 * @Date 2023/10/20 18:19
 * @Version 1.0
 **/
public interface MailTablesMapper {

    List<LinkedHashMap<String, Object>> selectTables(@Param("tableName") String tableName, @Param(Constants.WRAPPER) Wrapper<Object> queryWrapper);

    List<Map<String, Object>> selectGroupBy(@Param("tableName") String tableName,@Param("groupBy") String groupBy);

    Boolean truncateTable(String tableName);
}
