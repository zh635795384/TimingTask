package com.simatelex.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.simatelex.admin.model.RespBean;
import com.simatelex.admin.pojo.Mail;
import com.simatelex.admin.pojo.MailReceiver;
import com.simatelex.admin.query.MailQuery;
import com.simatelex.admin.service.IMailService;
import com.simatelex.admin.service.ISystemLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author Nancy Zhao
 * @Date 2023/09/07 18:10
 * @Version 1.0
 **/
@Slf4j
@Controller
@RequestMapping("mail")
public class MailController {

    @Resource
    ISystemLogService systemLogService;

    @Resource
    private IMailService mailService;

    @RequestMapping("index")
    public String index(Model model){
        return "mail/mail";
    }

    @RequestMapping("list")
    @ResponseBody
    public Map<String, Object> mailList(MailQuery mailQuery){
        log.info("邮件列表展示！");
        return mailService.mailList(mailQuery);
    }

    @RequestMapping("addOrUpdatePage")
    public String addOrUpdatePage(Integer id, Model model){
        model.addAttribute("mail", id != null ? mailService.getById(id) : null);
        log.info("进入邮件添加/更新页面！");
        return "mail/add_update";//返回页面
    }

    @RequestMapping("save")
    @ResponseBody
    public RespBean saveMail(Mail mail){
        mailService.saveMail(mail);
        log.info("邮箱"+mail.getTableName()+"信息添加成功！");
        systemLogService.addToLog(mail, "邮箱添加成功");
        return RespBean.success("数据添加成功");
    }

    @RequestMapping("update")
    @ResponseBody
    public RespBean updateMail(Mail mail,boolean isRun){
        mailService.updateMail(mail,isRun);
        log.info("邮箱"+mail.getTableName()+"信息更新成功！");
        systemLogService.addToLog(mail, "邮箱更新成功");
        return RespBean.success("数据更新成功");
    }

    @RequestMapping("delete")
    @ResponseBody
    public RespBean deleteMail(Integer[] ids, Integer flag) {
        mailService.deleteMail(ids, flag);
        log.info(flag.equals(1) ? "邮箱删除成功！" : "邮箱恢复成功");
        return RespBean.success(flag.equals(1) ? "数据删除成功" : "邮箱恢复成功");
    }

    @RequestMapping("runEmail")
    @ResponseBody
    public RespBean runEmail(boolean run) {
        mailService.runEmail(run);
        if (run) {
            systemLogService.addToLog("/mail/runEmail", "邮箱定时开始运行", "true");
            log.info("邮箱定时开始运行！");
            return RespBean.success("邮箱定时开始运行");
        } else {
            systemLogService.addToLog("/mail/runEmail", "邮箱定时停止运行！", "false");
            log.info("邮箱定时停止运行！");
            return RespBean.success("邮箱定时停止运行");
        }
    }

    @RequestMapping("addReceiverPage")
    public String addReceiverPage(MailQuery mailQuery, Model model){
        model.addAttribute("mailReceiver", JSON.toJSONString(mailService.receiverList(mailQuery).get("data")));
        model.addAttribute("mail", JSON.toJSONString(mailService.getById(mailQuery.getId())));
        log.info("进入邮件收件者添加/更新页面！");
        //返回页面
        return "mail/receiver";
    }

    @RequestMapping("receiverList")
    @ResponseBody
    public Map<String, Object> receiverList(MailQuery mailQuery){
        log.info("收件者列表展示！");
        return mailService.receiverList(mailQuery);
    }

    @RequestMapping("saveReceiver")
    @ResponseBody
//    public RespBean saveReceiver(@RequestBody List<MailReceiver> mailReceiverList){
    public RespBean saveReceiver(String mailReceiver,String mail){
//        List<MailReceiver> mailReceivers = JSONArray.parseArray(mailReceiver, MailReceiver.class);
        List<MailReceiver> mailReceiverList = JSONArray.parseArray(mailReceiver, MailReceiver.class);
        Mail mailHeader = JSONArray.parseObject(mail, Mail.class);
        mailService.saveReceiver(mailReceiverList,mailHeader);
        log.info("收件者"+mailHeader.getTableName()+"信息添加成功！");
        systemLogService.addToLog(mailHeader, "邮箱收件者"+mailHeader.getTableName()+"信息添加成功");
        return RespBean.success("数据添加成功");
    }

    @RequestMapping("deleteReceiver")
    @ResponseBody
    public RespBean deleteReceiver(Integer id) {
        mailService.deleteReceiver(id);
        return RespBean.success("收件者删除成功");
    }
}
