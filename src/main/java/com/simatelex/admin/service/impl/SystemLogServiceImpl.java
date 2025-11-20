package com.simatelex.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simatelex.admin.mappers.xfstd01.SysLogMapper;
import com.simatelex.admin.pojo.SysLog;
import com.simatelex.admin.service.ISystemLogService;
import com.simatelex.admin.util.DateUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Nancy Zhao
 * @Date 2023/01/11 11:53
 * @Version 1.0
 **/
@Service
public class SystemLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements ISystemLogService {

    /**
     * 添加到t_log中
     * @param uri
     * @param methodDescribe
     * @param params
     */
    @Override
//    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addToLog(String uri, String methodDescribe, String params) {
        String username = "email-admin";
        shareMethod(uri,methodDescribe,params,username);
    }

    /**
     * 添加到t_log中,多态
     * @param uri
     * @param methodDescribe
     * @param params
     * @param username
     */
    @Override
//    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addToLog(String uri, String methodDescribe, String params, String username) {
        shareMethod(uri,methodDescribe,params,username);
    }

    /**
     * 清除用户操作日志
     */
    @Override
//    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void clearLog() {
        this.baseMapper.delete(null);
    }

    /**
     * 共用方法
     * @param uri
     * @param methodDescribe
     * @param params
     * @param username
     */
    public void shareMethod(String uri, String methodDescribe, String params, String username){
        HttpServletRequest request = null;
        Map<String, String> mapLog = new HashMap<>();
//        System.out.println(mapLog);
        SysLog sysLog = new SysLog();
        sysLog.setUri(uri);
        sysLog.setMethod(mapLog.size()>0 ? mapLog.get("method") : null);
        sysLog.setMethodDescribe(methodDescribe);
        sysLog.setParams(params);
        sysLog.setUsername(username);
        sysLog.setOs(mapLog.size()>0 ? mapLog.get("os") : null);
        sysLog.setIp(mapLog.size()>0 ? mapLog.get("ip") : null);
        sysLog.setServerName(mapLog.size()>0 ? mapLog.get("serverName") : null);
        //日期转换
        sysLog.setCreateDate(DateUtil.getFormatDateTime(LocalDateTime.now()));
        sysLog.setBrowser(mapLog.get("browser"));
        this.baseMapper.insert(sysLog);
    }
}
