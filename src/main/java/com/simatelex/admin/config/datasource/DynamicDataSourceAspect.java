package com.simatelex.admin.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @Author Nancy Zhao
 * @Date 2023/10/21 10:11
 * @Version 1.0
 **/
@Aspect
@Order(-1)
@Component
@Slf4j
public class DynamicDataSourceAspect {

    @Pointcut("execution(* com.simatelex.admin.service.*.list*(..))")
    public void pointCut(){
    }

    /**
     * 执行方法前更换数据源
     * @param joinPoint 切点
     * @param targetDataSource  动态数据源
     */
    @Before("@annotation(targetDataSource)")
    public void doBefore(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        DataSourceKey dataSourceKey = targetDataSource.dataSourceKey();
        if (dataSourceKey == DataSourceKey.DB_OTHER) {
            log.info(String.format("设置数据源 %s", DataSourceKey.DB_OTHER));
            DynamicDataSourceContextHolder.set(DataSourceKey.DB_OTHER);
        } else if (dataSourceKey == DataSourceKey.DB_MASTER) {
            log.info(String.format("设置数据源 %s", DataSourceKey.DB_MASTER));
            DynamicDataSourceContextHolder.set(DataSourceKey.DB_MASTER);
        } else if (dataSourceKey == DataSourceKey.DB_XFBPS01) {
            log.info(String.format("设置数据源 %s", DataSourceKey.DB_XFBPS01));
            DynamicDataSourceContextHolder.set(DataSourceKey.DB_XFBPS01);
        } else {
            log.info(String.format("使用默认数据源 %s", DataSourceKey.DB_XFSTD01));
            DynamicDataSourceContextHolder.set(DataSourceKey.DB_XFSTD01);
        }
    }

    /**
     * 执行方法收清楚数据源设置
     * @param joinPoint 切点
     * @param targetDataSource  动态数据源
     */
    @After("@annotation(targetDataSource)")
    public void doAfter(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        log.info(String.format("当前数据源 %s 执行清理方法", targetDataSource.dataSourceKey()));
        DynamicDataSourceContextHolder.clear();
    }

    @Before(value = "pointCut()")
    public void doBeforeWithSlave(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获取当前切点方法对象
        Method method = methodSignature.getMethod();
        if (method.getDeclaringClass().isInterface()) {  //判断是否为接口方法
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(joinPoint.getSignature().getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                log.error("方法不存在！", e);
            }
        }
        if (null == method.getAnnotation(TargetDataSource.class)) {
            DynamicDataSourceContextHolder.setSlave();
        }
    }
}
