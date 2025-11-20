package com.simatelex.admin.mappers.xfbps01;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author Nancy Zhao
 * @Date 2023/10/20 18:19
 * @Version 1.0
 **/
public interface MailTablesMapper {

    List<Map<String, Object>> selectTables(@Param("tableName") String tableName,@Param(Constants.WRAPPER) Wrapper<Object> queryWrapper);

    List<Map<String, Object>> selectGroupBy(String tableName, String groupBy);

    Boolean truncateTable(String tableName);
}
