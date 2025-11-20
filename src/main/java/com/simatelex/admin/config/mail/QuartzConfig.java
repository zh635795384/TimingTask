package com.simatelex.admin.config.mail;

import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * @Author Nancy Zhao
 * @Date 2025/09/29 16:52
 * @Version 1.0
 **/
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail emailJobDetail() {
        return JobBuilder.newJob(EmailJob.class)
                .withIdentity("dailyEmailJob", "emailGroup")
                .usingJobData("toEmail", "recipient@example.com")
                .usingJobData("subject", "每日报告")
                .usingJobData("content", "这是自动发送的每日报告邮件")
                .usingJobData("isHtml", false)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger emailJobTrigger() {
        CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule("0 0 17 * * ?");

        return TriggerBuilder.newTrigger()
                .forJob(emailJobDetail())
                .withIdentity("emailTrigger", "emailGroup")
                .withSchedule(cronSchedule)
                .build();
    }

    // 配置SchedulerFactoryBean
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

        try {
            // 将ApplicationContext放入SchedulerContext
            schedulerFactory.setSchedulerContextAsMap(Map.of("applicationContext", applicationContext));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return schedulerFactory;
    }

    /*public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource){
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setOverwriteExistingJobs(true);
        factory.setJobFactory(springBeanJobFactory());

        // 配置支持有状态Job
        Properties props = new Properties();
        props.put("org.quartz.jobStore.dontSetAutoCommitFalse", "false");
        factory.setQuartzProperties(props);

        return factory;
    }*/
}
