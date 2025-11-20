package com.simatelex.admin.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author Nancy Zhao
 * @Date 2023/09/07 18:13
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("t_mail")
@Validated
public class Mail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String tableName;

    private String tablePrefix;

    private String tableSuffix;

    private String tableStyle;

    private String postPrefix;

    private String sendTarget;

    private String sendTime;

    private Boolean isRar;

    private String groupBy;

    private String fileName;

    private Boolean divExcel;

    private String outColumn;

    private String fonts;

    private Double fontSize;

    private Boolean insertRow;

    private String path;

    private String subject;

    private String sender;

    private String receiver;

    private String cc;

    private String bcc;

    private String attach;

    private String body;

    private String contact;

    private Boolean isExport;

    private Boolean isFtpUpload;

    private String ftpServer;

    private Integer ftpPort;

    private String ftpUserId;

    private String ftpUserPwd;

    private String ftpLocalPath;

    private Boolean isDel;

    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDate;
}
