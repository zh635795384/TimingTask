package com.simatelex.admin.config.mail;

import org.quartz.*;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author Nancy Zhao
 * @Date 2025/09/29 16:52
 * @Version 1.0
 **/
@Configuration
public class QuartzConfig {

    /*@Autowired
    private DataSource dataSource;*/

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

    /**
     * 自定义 JobFactory：让 Quartz Job 可以直接使用 Spring Bean（可选增强）
     */
    @Bean
    public SpringBeanJobFactory springBeanJobFactory(ApplicationContext applicationContext) {
        return new SpringBeanJobFactory() {
            @Override
            protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
                Object job = super.createJobInstance(bundle);
                applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
                return job;
            }
        };
    }

    /**
     * 核心调度器工厂
     * 1. 注入 Spring 上下文 → 解决 EmailJob 获取 ApplicationContext 空指针
     * 2. 开启任务覆盖、持久化、时区、自动启动
     * 3. 兼容动态添加/删除任务
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext) {
        /*SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

        // 1. 数据源（使用数据库持久化任务，防止重启任务丢失）
        schedulerFactory.setDataSource(dataSource);

        // 2. 自定义 Job 工厂，支持 Spring 依赖注入
        schedulerFactory.setJobFactory(springBeanJobFactory(applicationContext));

        // 3. 存在同名任务/触发器时直接覆盖
        schedulerFactory.setOverwriteExistingJobs(true);

        // 4. 项目启动后自动启动调度器
        schedulerFactory.setAutoStartup(true);

        // 5. 等待正在执行的任务完成再关闭（优雅停机）
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);

        // 6. 将 Spring 上下文放入 Quartz 全局上下文（关键！解决 Job 拿不到上下文）
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("applicationContext", applicationContext);
        schedulerFactory.setSchedulerContextAsMap(contextMap);

        // 7. Quartz 基础属性配置
        Properties props = new Properties();
        // 时区统一 Asia/Shanghai
        props.put("org.quartz.scheduler.timezone", "Asia/Shanghai");
        // 任务存储模式：数据库持久化
        props.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        props.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        // 集群配置（多实例部署开启，单实例也无害）
        props.put("org.quartz.jobStore.isClustered", "true");
        props.put("org.quartz.jobStore.clusterCheckinInterval", "10000");
        // 线程池配置
        props.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.put("org.quartz.threadPool.threadCount", "10");
        props.put("org.quartz.threadPool.threadPriority", "5");

        schedulerFactory.setQuartzProperties(props);

        return schedulerFactory;*/
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

        try {
            // 将ApplicationContext放入SchedulerContext
            schedulerFactory.setSchedulerContextAsMap(Map.of("applicationContext", applicationContext));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return schedulerFactory;
    }

    /*@Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) {
        return schedulerFactoryBean.getObject();
    }*/

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
