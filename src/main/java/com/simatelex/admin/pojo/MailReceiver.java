package com.simatelex.admin.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * @Author Nancy Zhao
 * @Date 2026/06/05 13:31
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("t_mail_receiver")
@Validated
public class MailReceiver {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    //报表id
    private Integer mailId;

    //报表名
    @TableField(exist = false)
    private String tableName;

    //导出路径
    @TableField(exist = false)
    private String path;

    //分组值
    private String groupKey;

    //收件者
    private String receiver;

    //抄送者
    private String cc;

    //秘密抄送
    private String bcc;

    //是否删除
    private Boolean isDel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDate;
}
