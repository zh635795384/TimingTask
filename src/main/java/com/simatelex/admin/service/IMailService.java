package com.simatelex.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simatelex.admin.pojo.Mail;
import com.simatelex.admin.query.MailQuery;

import java.util.Map;

/**
 * @Author Nancy Zhao
 * @Date 2025/09/29 16:38
 * @Version 1.0
 **/
public interface IMailService extends IService<Mail> {
    Map<String, Object> mailList(MailQuery mailQuery);

    void saveMail(Mail mail);

    void updateMail(Mail mail, boolean isRun);

    void deleteMail(Integer[] ids);

    void sendMail(Mail mail);

    void runEmail(boolean run);
}
