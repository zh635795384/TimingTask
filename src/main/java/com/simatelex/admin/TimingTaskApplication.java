package com.simatelex.admin;


import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.simatelex.admin.config.datasource.DataSourceConfig1;
import com.simatelex.admin.config.datasource.DataSourceConfig2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 如果需要打war包
 * 通过继承SpringBootServletInitializer类并重写configure方法来实现，在部署项目时指定外部Tomcat读取项目入口方法
 */

@SpringBootApplication(exclude = {MybatisPlusAutoConfiguration.class})//(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})//(exclude = {SecurityAutoConfiguration.class})
@Import({DataSourceConfig1.class, DataSourceConfig2.class})
public class TimingTaskApplication { //extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TimingTaskApplication.class,args);
    }
}
