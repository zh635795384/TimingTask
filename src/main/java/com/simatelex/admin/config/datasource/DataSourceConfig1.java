package com.simatelex.admin.config.datasource;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.simatelex.admin.config.MyMetaObjectHandler;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @Author Nancy Zhao
 * @Date 2023/10/16 10:57
 * @Version 1.0
 **/
//表示这个类为一个配置类
@Slf4j
@Configuration
@MapperScan(basePackages = "com.simatelex.admin.mappers.xfstd01", sqlSessionFactoryRef = "xfstd01SqlSessionFactory")
public class DataSourceConfig1 {
    @Value("${spring.datasource.xfstd01.driver-class-name}")
    String driverClass;

    @Value("${spring.datasource.xfstd01.url}")
    String url;

    @Value("${spring.datasource.xfstd01.username}")
    String username;

    @Value("${spring.datasource.xfstd01.password}")
    String password;

    @Value("${spring.datasource.xfstd01.hikari.maximum-pool-size:10}")
    int maximumPoolSize;

    @Value("${spring.datasource.xfstd01.hikari.idle-timeout:240000}")
    long idleTimeout;

    @Value("${spring.datasource.xfstd01.hikari.max-lifetime:540000}")
    long maxLifetime;

    @Value("${spring.datasource.xfstd01.hikari.validation-timeout:3000}")
    long validationTimeout;

    @Resource
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    @Primary // 表示这个数据源是默认数据源, 这个注解必须要加，因为不加的话spring将分不清楚那个为主数据源（默认数据源）
    @Bean("xfstd01DataSource")  //将这个对象放入Spring容器中
    public DataSource getXfstd01DataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("xfstd01");
        dataSource.setDriverClassName(driverClass);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setMaxLifetime(maxLifetime);
        dataSource.setValidationTimeout(validationTimeout);
        dataSource.setConnectionTestQuery("SELECT 1");
        log.info("xfstd01 DataSource init, jdbcUrl={}, username={}", url, username);
        return dataSource;
    }

    @Primary
    @Bean("xfstd01SqlSessionFactory")
    //@Qualifier表示查找spring容器中名字为xfstd01DataSource的对象
    public SqlSessionFactory xfstd01SqlSessionFactory(@Qualifier("xfstd01DataSource") DataSource dataSource) throws Exception {
//        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        //要将此bean换成mybatis的，不然报错
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        //必须加上这个全局配置，不然自动匹配日期无法工作
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
        globalConfig.setBanner(false);
        bean.setDataSource(dataSource);
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/xfstd01/*Mapper.xml"));
        bean.setGlobalConfig(globalConfig);
        //配置多数据源需要设置驼峰规则，否则不生效
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        bean.setPlugins(mybatisPlusInterceptor);  //需设置分页插件，不然会卡死
        bean.setConfiguration(configuration);
        return bean.getObject();
    }

    // 创建事务管理器1
    @Primary
    @Bean(name = "xfstd01Manager")
    public PlatformTransactionManager xfstd01Manager(@Qualifier("xfstd01DataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean("xfstd01SqlSessionTemplate")
    public SqlSessionTemplate xfstd01SqlSessionTemplate(@Qualifier("xfstd01SqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
