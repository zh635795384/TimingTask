package com.simatelex.admin.config.mail;

import com.simatelex.admin.pojo.Mail;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Nancy Zhao
 * @Date 2025/09/29 16:54
 * @Version 1.0
 **/
public class SetCron {
    public static String getCron(Mail mail) {   //暂只支持每天，其他待增加
        // sendTime为空直接返回10分钟一次的cron
        if (mail == null || mail.getSendTime() == null) {
            // 每10分钟执行：秒 分 时 日 月 星期 年
            return "0 */10 * * * ?";
        }
        //秒
        String second = "*";
        //分
        String minute = "*";
        //时
        String hour = "*";
        //天
        String day = "*";
        //月
        String month = "*";
        //星期
        String week = "?";
        //年
        String year = "";
        List<String> weekList = new ArrayList<>();
        List<String> monthList = new ArrayList<>();
        //将sendTime分开
        String[] splitSendTime = mail.getSendTime().split(";");
        List<String> cronList = new ArrayList<>();
        //有可能是5;2;8:00
        for (int j = 0; j < splitSendTime.length; j++) {
            //如果包括：代表是时间
            if (splitSendTime[j].contains(":")) {
                String[] timeArr = splitSendTime[j].split(":");
                hour = timeArr[0];
                minute = timeArr[1];
                //兼容只有时分没有秒的情况，防止数组越界
                second = timeArr.length >= 3 ? timeArr[2] : "0";
            }
            //证明不是时间,适用于每星期、每月、每年
            if (!splitSendTime[j].contains(":")) {
                if (mail.getSendTarget().equals("每周")) {
                    weekList.add(splitSendTime[j]);
                    week = StringUtils.join(weekList, ",");
                }
                if (mail.getSendTarget().equals("每月")) {
                    monthList.add(splitSendTime[j]);
                    month = StringUtils.join(monthList, ",");
                }
                if (mail.getSendTarget().equals("每年")) {
                    year = splitSendTime[j];
                    //大于2代表输入了年份
                    if (splitSendTime[j].indexOf(";")>2) {
                        year = "*";
                    }
                }
            }
            //拼接cron，年为空则不拼接尾部空格
            String cronItem;
            if (StringUtils.isBlank(year)) {
                cronItem = second +" "+ minute + " "+ hour +" " + day +" " + month + " " + week;
            } else {
                cronItem = second +" "+ minute + " "+ hour +" " + day +" " + month + " " + week + " " + year;
            }
            cronList.add(cronItem);
        }
        return StringUtils.join(cronList,";");
    }
}
