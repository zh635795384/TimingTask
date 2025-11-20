package com.simatelex.admin.config.datasource;

import java.lang.annotation.*;

/**
 * @Author Nancy Zhao
 * 为了不影响业务代码而实现数据源切换，使用AOP切换数据源，为了准确的知道哪个地方需要切换哪个数据源，使用自定义注解
 * @Date 2023/10/21 10:08
 * @Version 1.0
 **/
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TargetDataSource {
    DataSourceKey dataSourceKey() default DataSourceKey.DB_XFSTD01;

}
