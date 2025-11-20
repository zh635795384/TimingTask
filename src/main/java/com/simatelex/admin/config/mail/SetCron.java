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
        String second = "*";    //秒
        String minute = "*";    //分
        String hour = "*";      //时
        String day = "*";       //天
        String month = "*";     //月
        String week = "?";      //星期
        String year = "";      //年
        List<String> weekList = new ArrayList<>();
        List<String> monthList = new ArrayList<>();
        String[] splitSendTime = mail.getSendTime().split(";"); //将sendTime分开
        List<String> cronList = new ArrayList<>();
        for (int j = 0; j < splitSendTime.length; j++) {    //有可能是5;2;8:00
            if (splitSendTime[j].contains(":")) {   //如果包括：代表是时间
                hour = splitSendTime[j].split(":")[0];
                minute = splitSendTime[j].split(":")[1];
                second = splitSendTime[j].split(":")[2];
            }
            if (!splitSendTime[j].contains(":")) {  //证明不是时间,适用于每星期、每月、每年
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
                    if (splitSendTime[j].indexOf(";")>2) {//大于2代表输入了年份
                        year = "*";
                    }
                }
            }
            cronList.add(second +" "+ minute + " "+ hour +" " + day +" " + month + " " + week + " " + year);
        }
        return StringUtils.join(cronList,";");
    }
}
