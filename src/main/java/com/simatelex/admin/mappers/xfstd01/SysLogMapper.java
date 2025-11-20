package com.simatelex.admin.mappers.xfstd01;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simatelex.admin.pojo.SysLog;

/**
 * @Author Nancy Zhao
 * @Date 2022/11/08 11:51
 * @Version 1.0
 **/
public interface SysLogMapper extends BaseMapper<SysLog> {
    void InsertSysLog(SysLog sysLog);
}
