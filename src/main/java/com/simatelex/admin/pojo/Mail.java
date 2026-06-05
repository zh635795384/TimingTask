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

    //报表名
    private String tableName;

    //别名
    private String tablePrefix;

    //后缀(可增加自定义后缀)
    private String tableSuffix;

    //报表样式
    private String tableStyle;

    //提交后缀
    private String postPrefix;

    //发送周期
    private String sendTarget;

    //发送时间
    private String sendTime;

    //是否压缩
    private Boolean isRar;

    //分组
    private String groupBy;

    //导出的EXCEL名称
    private String fileName;

    //是否分sheet
    private Boolean divExcel;

    //导出的字段，可自定义
    private String outColumn;

    //自定义导出的字体
    private String fonts;

    //自定义导出的字大小
    private Double fontSize;

    //是否插入空白行
    private Boolean insertRow;

    //导出的路径
    private String path;

    //发送邮件的主旨
    private String subject;

    //发件者
    private String sender;

    //附件
    private String attach;

    //正文文本
    private String body;

    //合并的报表
    private String contact;

    //是否只导出
    private Boolean isExport;

    //是否ftp上传
    private Boolean isFtpUpload;

    //ftp服务器
    private String ftpServer;

    //ftp端口
    private Integer ftpPort;

    //ftp连接用户名
    private String ftpUserId;

    //ftp连接用户密码
    private String ftpUserPwd;

    //ftp上传的本地路径
    private String ftpLocalPath;

    //是否空主旨发送
    private Boolean emptyAttach;

    private Boolean isDel;

    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDate;
}
