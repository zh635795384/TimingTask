package com.simatelex.admin.service;

/**
 * @Author Nancy Zhao
 * @Date 2023/01/11 11:52
 * @Version 1.0
 **/
public interface ISystemLogService {

    /**
     * 添加日志到数据库
     * @param uri
     * @param methodDescribe
     * @param params
     */
    void addToLog(String uri, String methodDescribe, String params);

    /**
     * 多态，添加日志到数据库
     * @param uri
     * @param methodDescribe
     * @param params
     * @param username
     */
    void addToLog(String uri, String methodDescribe, String params, String username);

    /**
     * 清除用户操作日志
     */
    void clearLog();
}
