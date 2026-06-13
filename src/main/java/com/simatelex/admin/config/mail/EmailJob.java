package com.simatelex.admin.config.mail;

import com.simatelex.admin.pojo.Mail;
import com.simatelex.admin.service.IMailService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Date;

/**
 * @Author Nancy Zhao
 * @Date 2025/09/29 16:46
 * @Version 1.0
 **/
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EmailJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(EmailJob.class);

    //定义重试次数的键
    private static final String RETRY_COUNT = "retryCount";

    private static final String LAST_ERROR = "lastError";

    //最大重试次数
    private static final int MAX_RETRIES = 3;

    // 重试间隔时间（毫秒）
    private static final long[] RETRY_INTERVALS = {30000, 60000, 120000}; //递增重试间隔：30s,1分钟,2分钟

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 强制打印入口日志，判断任务是否被触发
        logger.info("===== Quartz 邮件任务【{}】进入 execute 方法 =====", context.getJobDetail().getKey().getName());
        // 从JobDataMap获取参数
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        //获取当前重试次数，默认为0
        int count = jobDataMap.getIntValue(RETRY_COUNT);
        logger.info("开始执行邮件任务，重试: {}/{}", count, MAX_RETRIES);
        try {
            //如果已超过最大重试次数，则不再重试
            if (count >= MAX_RETRIES) {
                handleMaxRetriesReached(jobDataMap, context);
                return;
            }

            // 业务逻辑执行
            executeEmailBusinessLogic(context, jobDataMap);

            // 成功完成，重置状态
            resetRetryState(jobDataMap);
            logger.info("===== 邮件任务【{}】执行成功 =====", context.getJobDetail().getKey().getName());
        } catch (Exception e) {
            logger.error("===== 邮件任务【{}】执行异常 =====", context.getJobDetail().getKey().getName(), e);
            handleExecutionFailure(e, jobDataMap, count, context);
        }
    }

    /**
     * 执行邮件业务逻辑
     */
    private void executeEmailBusinessLogic(JobExecutionContext context, JobDataMap jobDataMap) throws Exception {
        Mail mail = (Mail) jobDataMap.get("mail");
        if (mail == null) {
            throw new IllegalArgumentException("邮件对象不能为null");
        }

        ApplicationContext applicationContext = (ApplicationContext)
                context.getScheduler().getContext().get("applicationContext");
        IMailService mailService = applicationContext.getBean(IMailService.class);

        mailService.sendMail(mail);
    }

    /**
     * 处理达到最大重试次数的情况
     */
    private void handleMaxRetriesReached(JobDataMap jobDataMap, JobExecutionContext context) throws JobExecutionException {
        String lastError = jobDataMap.getString(LAST_ERROR);
        String errorMsg = String.format("邮件发送失败，已达到最大重试次数 %d。最后错误: %s",
                MAX_RETRIES, lastError);

        logger.error(errorMsg);

        // 发送告警通知（可选）
        sendAlertNotification(context, errorMsg);

        // 重置状态
        resetRetryState(jobDataMap);

        throw new JobExecutionException(errorMsg);
    }

    /**
     * 处理执行失败
     */
    private void handleExecutionFailure(Exception e, JobDataMap jobDataMap, int count, JobExecutionContext context) throws JobExecutionException {
        // 更新重试状态
        count++;
        jobDataMap.put(RETRY_COUNT, count);
        jobDataMap.put(LAST_ERROR, e.getMessage());

        logger.warn("邮件发送第 {} 次失败，错误: {}", count, e.getMessage());

        JobExecutionException retryException = new JobExecutionException(e);

        if (count < MAX_RETRIES) {
            // 设置延迟重试
            setupSmartRetry(retryException, count);
            logger.info("将在 {} 秒后重试", RETRY_INTERVALS[count-1]/1000);
        } else {
            logger.error("达到最大重试次数，停止重试");
        }

        throw retryException;
    }

    /**
     * 智能重试策略
     */
    private void setupSmartRetry(JobExecutionException retryException, int retryCount) {
        try {
            // 使用递增的重试间隔
            long retryInterval = RETRY_INTERVALS[Math.min(retryCount, RETRY_INTERVALS.length) - 1];
            logger.info("等待 {} 毫秒后进行第 {} 次重试", retryInterval, retryCount + 1);

            Thread.sleep(retryInterval);
            retryException.setRefireImmediately(true);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            retryException.setRefireImmediately(false);
        }
    }

    /**
     * 重置重试状态
     */
    private void resetRetryState(JobDataMap jobDataMap) {
        jobDataMap.put(RETRY_COUNT, 0);
        jobDataMap.put(LAST_ERROR, "");
    }

    /**
     * 发送告警通知（可选）
     */
    private void sendAlertNotification(JobExecutionContext context, String errorMessage) {
        // 这里可以集成您的告警系统，如发送邮件、短信、钉钉通知等
        logger.error("发送告警通知: {}", errorMessage);

        try {
            // 示例：通过相同的邮件服务发送告警
            ApplicationContext appContext = (ApplicationContext)
                    context.getScheduler().getContext().get("applicationContext");

            // 这里可以获取告警服务发送通知
            // AlertService alertService = appContext.getBean(AlertService.class);
            // alertService.sendAlert("邮件发送任务失败", errorMessage);

        } catch (Exception e) {
            logger.error("发送告警通知失败", e);
        }
    }
}
