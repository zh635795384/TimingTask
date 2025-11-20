package com.simatelex.admin.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

/**
 * @Author Nancy Zhao
 * @Date 2022/11/08 11:45
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("t_log")
@Validated
public class SysLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private String uri;

    private String method;

    private String methodDescribe;

    private String params;

    private String username;

    private String os;

    private String ip;

    private String createDate;

    private String browser;

    private String serverName;
}
