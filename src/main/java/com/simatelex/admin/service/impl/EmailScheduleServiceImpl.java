package com.simatelex.admin.service.impl;

import com.simatelex.admin.config.mail.EmailJob;
import com.simatelex.admin.pojo.Mail;
import com.simatelex.admin.service.IEmailScheduleService;
import com.simatelex.admin.service.IMailService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @Author Nancy Zhao
 * @Date 2025/09/29 17:04
 * @Version 1.0
 **/
@Slf4j
@Service
public class EmailScheduleServiceImpl implements IEmailScheduleService {

    @Autowired
    private Scheduler scheduler;

    @Override
    public void scheduleEmailJob(String jobName, String jobGroup, Mail mail, String cron) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("mail", mail);

        System.out.println("jobKey:"+jobKey);
        System.out.println("jobName:"+jobName);
        System.out.println("jobGroup:"+jobGroup);

        // 第一步：存在旧任务 → 先删除（连带触发器一起清掉）
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        // 创建JobDetail
        JobDetail jobDetail = JobBuilder.newJob(EmailJob.class)
                .usingJobData(jobDataMap)
                .withIdentity(jobName, jobGroup)
                //初始化重试计数
                .usingJobData("retryCount", 0)
                // 初始化错误信息
                .usingJobData("lastError", "")
                .storeDurably()
                .build();

        scheduler.addJob(jobDetail, true);

        // 创建Trigger,可创建多个Trigger；拆分多Cron表达式，循环创建触发器
        String[] splitCron = cron.split(";");
        for (int i = 0; i < splitCron.length; i++) {
            String realCron = splitCron[i].trim();
            TriggerKey triggerKey = new TriggerKey("trigger"+(i+1)+"_" + jobName, jobGroup);
            System.out.println("triggerKey:"+triggerKey);
            // 清同名旧触发器
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
            }
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(realCron)
                            .inTimeZone(TimeZone.getTimeZone("Asia/Shanghai")))
                    .startNow() //立即启动调度
                    .forJob(jobDetail)
                    .build();
            // 调度任务
//        scheduler.scheduleJob(jobDetail, trigger);
            //绑定调度
            scheduler.scheduleJob(trigger);
            log.info("触发器 {} 调度成功，Cron:{}", triggerKey, realCron);
        }
        // ========= 新增：手动立即执行一次 =========
        log.info("任务注册完成，手动触发首次执行");
        scheduler.triggerJob(jobKey);
    }

    @Override
    public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.pauseJob(jobKey);
    }

    @Override
    public void resumeJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.resumeJob(jobKey);
    }

    @Override
    public void deleteJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.deleteJob(jobKey);
    }

    @Override
    public void standby() {
        try {
            scheduler.standby();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startScheduler() throws SchedulerException {
        // 如果是待机状态，恢复运行；如果已关闭，重新启动
        if (scheduler.isInStandbyMode()) {
            scheduler.start();
        } else if (scheduler.isShutdown()) {
            scheduler.start();
        }
    }

    @Override
    public boolean isStandbyMode() throws SchedulerException {
        return scheduler.isInStandbyMode();
    }

    @Override
    public void shutdownScheduler() throws SchedulerException {
        if (!scheduler.isShutdown()) {
            // true = 等待正在执行的任务完成再关闭
            scheduler.shutdown(true);
        }
    }

    /**
     * 修改任务
     * @param jobName
     * @param cron
     */
    public void modifyJob(String jobName, String jobGroup, String cron, Mail mail) {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroup);
            //首先验证job是否存在
            if (!scheduler.checkExists(jobKey)) {
                throw new RuntimeException("Job不存在: " + jobKey);
            }
            // 刷新 JobDataMap 中的 Mail，避免仍发送旧配置
            JobDetail oldDetail = scheduler.getJobDetail(jobKey);
            if (oldDetail != null && mail != null) {
                JobDataMap dataMap = new JobDataMap(oldDetail.getJobDataMap());
                dataMap.put("mail", mail);
                dataMap.put("retryCount", 0);
                dataMap.put("lastError", "");
                JobDetail newDetail = oldDetail.getJobBuilder()
                        .usingJobData(dataMap)
                        .storeDurably()
                        .build();
                scheduler.addJob(newDetail, true);
            }
            //删除旧的Trigger
            List<? extends Trigger> oldTriggers = scheduler.getTriggersOfJob(jobKey);
            for (Trigger trigger : oldTriggers) {
                scheduler.unscheduleJob(trigger.getKey());
                System.out.println("删除旧触发器:"+ trigger.getKey());
            }
            //等待一小段时间确保删除完成
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("删除触发器过程被中断", e);
            }
            //验证并分割Cron表达式
            String[] cronExpressions = cron.split(";");
            if (cronExpressions.length == 0) {
                throw new RuntimeException("Cron表达式不能为空");
            }
            // 创建新的Trigger（Job仍然存在，因为它是持久的）
            List<Trigger> newTriggers = new ArrayList<>();
            for (int i = 0; i < cronExpressions.length; i++) {
                String realCron = cronExpressions[i].trim();
                //验证Cron表达式
                if (!isValidCronExpression(realCron)) {
                    throw new RuntimeException("无效的Cron表达式"+ realCron);
                }
                TriggerKey triggerKey = new TriggerKey("trigger"+(i+1)+"_" + jobName, jobGroup);
                System.out.println("创建新触发器:"+triggerKey+", Cron:"+ realCron);
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(triggerKey)
                        .forJob(jobKey) //明确关联到jobKey
                        .withSchedule(CronScheduleBuilder.cronSchedule(realCron)
                                .inTimeZone(TimeZone.getTimeZone("Asia/Shanghai")))
                        .startNow() //确保立即开始调度,会马上执行一次调度
                        .build();
                newTriggers.add(trigger);
            }
            //批量调度新触发器
            for (Trigger trigger : newTriggers) {
                try {
                    Date nextFireTime = scheduler.scheduleJob(trigger);
                    System.out.println("触发器调度成功: " + trigger.getKey() + ", 下次触发: " + nextFireTime);
                } catch (SchedulerException e) {
                    // 如果单个触发器失败，记录错误但继续处理其他触发器
                    System.err.println("调度触发器失败 " + trigger.getKey() + ": " + e.getMessage());
                    // 可以选择回滚已创建的触发器
                    rollbackCreatedTriggers(newTriggers, trigger);
                }
            }
            System.out.println("所有触发器更新完成");
        } catch (SchedulerException e) {
            //异常时确保调度器重新启动
            try {
                if (!scheduler.isShutdown()) {
                    scheduler.start();
                }
            } catch (SchedulerException se) {
                System.err.println("重新启动调度器失败: " + se.getMessage());
            }
            throw new RuntimeException("更新调度失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证Cron表达式是否有效
     */
    private boolean isValidCronExpression(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return true;
        } catch (Exception e) {
            System.err.println("无效的Cron表达式: " + cronExpression + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * 回滚已创建的触发器（如果部分触发器创建失败）
     */
    private void rollbackCreatedTriggers(List<Trigger> createdTriggers, Trigger failedTrigger) {
        try {
            for (Trigger trigger : createdTriggers) {
                if (!trigger.getKey().equals(failedTrigger.getKey())) {
                    scheduler.unscheduleJob(trigger.getKey());
                    System.out.println("回滚触发器: " + trigger.getKey());
                }
            }
        } catch (SchedulerException e) {
            System.err.println("回滚触发器失败: " + e.getMessage());
        }
    }
}
