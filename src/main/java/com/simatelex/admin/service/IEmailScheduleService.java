package com.simatelex.admin.service;

import com.simatelex.admin.pojo.Mail;
import org.quartz.SchedulerException;

/**
 * @Author Nancy Zhao
 * @Date 2025/09/29 17:01
 * @Version 1.0
 **/
public interface IEmailScheduleService {
    void scheduleEmailJob(String jobName, String jobGroup, Mail mail,String cron) throws SchedulerException;

    void pauseJob(String jobName,String jobGroup) throws SchedulerException;

    void resumeJob(String jobName, String jobGroup) throws SchedulerException;

    void deleteJob(String jobName, String jobGroup) throws SchedulerException;

    void standby();

    /** 启动/唤醒调度器 **/
    void startScheduler() throws SchedulerException;

    /** 判断调度器是否处于待机状态 **/
    boolean isStandbyMode() throws SchedulerException;

    /** 彻底关闭调度器 **/
    void shutdownScheduler() throws SchedulerException;
}
