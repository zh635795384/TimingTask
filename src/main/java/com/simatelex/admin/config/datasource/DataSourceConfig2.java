package com.simatelex.admin.config.datasource;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.simatelex.admin.config.MyMetaObjectHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @Author Nancy Zhao
 * @Date 2023/10/16 11:02
 * @Version 1.0
 **/
@Configuration
@MapperScan(basePackages = "com.simatelex.admin.mappers.xfbps01", sqlSessionFactoryRef = "xfbps01SqlSessionFactory")
public class DataSourceConfig2 {
    @Value("${spring.datasource.xfbps01.driver-class-name}")
    String driverClass;

    @Value("${spring.datasource.xfbps01.url}")
    String url;

    @Value("${spring.datasource.xfbps01.username}")
    String username;

    @Value("${spring.datasource.xfbps01.password}")
    String password;

    @Resource
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    @Bean(name = "xfbps01DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.xfbps01")
    public DataSource getXfbps01DataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
//        return DataSourceBuilder.create().build();
    }

    @Bean("xfbps01SqlSessionFactory")
    public SqlSessionFactory xfbps01SqlSessionFactory(@Qualifier("xfbps01DataSource") DataSource dataSource) throws Exception {
        //要将此bean换成mybatis的，不然报错
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
        globalConfig.setBanner(false);
        bean.setDataSource(dataSource);
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/xfbps01/*Mapper.xml"));
        bean.setGlobalConfig(globalConfig);
        //配置多数据源需要设置驼峰规则，否则不生效
        MybatisConfiguration configuration=new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCallSettersOnNulls(true);
        bean.setPlugins(mybatisPlusInterceptor);
        bean.setConfiguration(configuration);
        return bean.getObject();
    }

    @Bean("xfbps01SqlSessionTemplate")
    public SqlSessionTemplate xfbps01SqlSessionTemplate(@Qualifier("xfbps01SqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    // 创建事务管理器1
    @Primary
    @Bean(name = "xfbps01Manager")
    public PlatformTransactionManager xfbps01Manager(@Qualifier("xfbps01DataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /*@Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.SQL_SERVER2005));
        return interceptor;
    }*/

}
