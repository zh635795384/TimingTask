package com.simatelex.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
//import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 将日期时间自动插入到数据库中
 */
@Component
//@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    //使用mp进行添加操作这个方法执行
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("create_date",LocalDateTime.now(),metaObject);
        this.setFieldValByName("update_date",LocalDateTime.now(),metaObject);
        this.setFieldValByName("createDate",LocalDateTime.now(),metaObject);
        this.setFieldValByName("updateDate",LocalDateTime.now(),metaObject);
//        this.setFieldValByName("last_used", LocalDateTime.now(),metaObject);
        this.setFieldValByName("crtDate", LocalDateTime.now(),metaObject);
//        this.setFieldValByName("listTime", LocalDateTime.now(),metaObject);
        this.setFieldValByName("downloadTime", LocalDateTime.now(),metaObject);
        this.setFieldValByName("lineCreateDate", LocalDateTime.now(),metaObject);
        this.setFieldValByName("inputDate", LocalDateTime.now(),metaObject);
    }
    //使用mp进行修改操作这个方法执行
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("update_date", LocalDateTime.now(),metaObject);
        this.setFieldValByName("last_used",LocalDateTime.now(),metaObject);
        this.setFieldValByName("update_time", LocalDateTime.now(),metaObject);
        this.setFieldValByName("list_time", LocalDateTime.now(),metaObject);
        this.setFieldValByName("listTime", LocalDateTime.now(),metaObject);
        this.setFieldValByName("lineUpdateDate", LocalDateTime.now(),metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(),metaObject);
        this.setFieldValByName("updateDate", LocalDateTime.now(),metaObject);
    }
}
