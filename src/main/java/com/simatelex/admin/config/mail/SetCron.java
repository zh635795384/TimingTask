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
    //暂只支持每天，其他待增加
    public static String getCron(Mail mail) {
        // sendTime为空直接返回10分钟一次的cron
        if (mail != null && StringUtils.isBlank(mail.getSendTime())) {
            // 每10分钟执行：秒 分 时 日 月 星期 年，这个意思是先执行一次，再间隔10分钟执行
            return "0 0/10 * * * ?";    //把0/10中的0换成*，这个意思等10分钟再间隔10分钟执行
            //10秒钟执行一次
            //return "0/10 * * * * ?";
        }
        // 默认
        String day = "*";
        String month = "*";
        String week = "?";
        String year = "";

        List<String> weekList = new ArrayList<>();
        List<String> dayList = new ArrayList<>();
        List<String> monthList = new ArrayList<>();
        List<String> yearList = new ArrayList<>();
        List<String> timeList = new ArrayList<>();

        // 拆分 sendTime，格式：类型部分;时间部分
        String[] parts = mail.getSendTime().split(";");
        String typePart = "";
        String timePart = "";

        if (parts.length > 1) {
            // 有分号，格式：类型;时间
            typePart = parts[0];
            timePart = parts[1];
        } else {
            // 没有分号，判断是时间还是类型
            if (parts[0].contains(":")) {
                timePart = parts[0];   // 只有时间（如每天 10:00）
            } else {
                typePart = parts[0];   // 只有类型（理论上不应出现，可给默认时间）
            }
        }

        // 解析类型
        for (String type : typePart.split(",")) {
            type = type.trim();
            if (type.isEmpty()) continue;
            if ("每星期".equals(mail.getSendTarget())) {
                weekList.add(convertWeekToQuartz(type));
                day = "?";
            } else if ("每月".equals(mail.getSendTarget())) {
                dayList.add(type);
                week = "?";
            } else if ("每年".equals(mail.getSendTarget())) {
                yearList.add(type);
            }
        }
        if (!weekList.isEmpty()) week = String.join(",", weekList);
        if (!dayList.isEmpty()) day = String.join(",", dayList);
        if (!yearList.isEmpty()) year = String.join(",", yearList);

        // 解析时间
        if (StringUtils.isNotBlank(timePart)) {
            for (String t : timePart.split(",")) {
                if (StringUtils.isNotBlank(t)) timeList.add(t.trim());
            }
        }

        // 生成 cron（每个时间点一条，用 ; 拼接）
        List<String> cronList = new ArrayList<>();
        for (String t : timeList) {
            String[] timeArr = t.split(":");
            String hour = timeArr.length > 0 ? timeArr[0] : "0";
            String minute = timeArr.length > 1 ? timeArr[1] : "0";
            String second = timeArr.length > 2 ? timeArr[2] : "0";

            String cron;
            if (StringUtils.isBlank(year)) {
                cron = String.join(" ", second, minute, hour, day, month, week);
            } else {
                cron = String.join(" ", second, minute, hour, day, month, week, year);
            }
            cronList.add(cron);
        }
        return String.join(";", cronList);
    }

    private static String convertWeekToQuartz(String userWeek) {
        List<String> result = new ArrayList<>();
        for (String w : userWeek.split(",")) {
            int n = Integer.parseInt(w.trim());
            // 用户 1=周一...7=周日 → Quartz 2=周一...1=周日
            result.add(String.valueOf(n == 7 ? 1 : n + 1));
        }
        return String.join(",", result);
    }
    /*public static String getCron(Mail mail) {
        // sendTime为空直接返回10分钟一次的cron
        if (mail != null && StringUtils.isBlank(mail.getSendTime())) {
            // 每10分钟执行：秒 分 时 日 月 星期 年，这个意思是先执行一次，再间隔10分钟执行
            return "0 0/10 * * * ?";    //把0/10中的0换成*，这个意思等10分钟再间隔10分钟执行
            //10秒钟执行一次
            //return "0/10 * * * * ?";
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
        List<String> dayList = new ArrayList<>();
        List<String> monthList = new ArrayList<>();
        List<String> yearList = new ArrayList<>();
        //拼接cron，年为空则不拼接尾部空格
        String cronItem;
        //将sendTime分开
        String[] typeAndTime = mail.getSendTime().split(";");
        String[] sendTimeList = new String[0];
        String[] sendTypeList = new String[0];
        if (typeAndTime.length > 1) {
            sendTimeList = typeAndTime[1].split(",");
            sendTypeList = typeAndTime[0].split(",");
        } else {
            if (typeAndTime[0].contains(":")) {
                sendTimeList =  typeAndTime[0].split(",");
            } else {
                sendTypeList = typeAndTime[0].split(",");
            }
        }
        List<String> cronList = new ArrayList<>();
        for (String sendType : sendTypeList) {
            if (mail.getSendTarget().equals("每星期")) {
                weekList.add(sendType);
                day = "?";
            }
            if (mail.getSendTarget().equals("每月")) {
                dayList.add(sendType);
                week = "?";
            }
            if (mail.getSendTarget().equals("每年")) {
                yearList.add(sendType);
            }
        }
        if (!weekList.isEmpty()) week = StringUtils.join(weekList, ",");
        if (!dayList.isEmpty()) day = StringUtils.join(dayList, ",");
        if (!yearList.isEmpty()) year = StringUtils.join(yearList, ",");
        //有可能是5;2;8:00
        for (String sendTime : sendTimeList) {
            String[] timeArr = sendTime.split(":");
            hour = timeArr[0];
            minute = timeArr[1];
            //兼容只有时分没有秒的情况，防止数组越界
            second = timeArr.length >= 3 ? timeArr[2] : "0";
            //证明不是时间,适用于每星期、每月、每年
        }
        if (StringUtils.isBlank(year)) {
            cronItem = second +" "+ minute + " "+ hour +" " + day +" " + month + " " + week;
        } else {
            cronItem = second +" "+ minute + " "+ hour +" " + day +" " + month + " " + week + " " + year;
        }
        cronList.add(cronItem);
        return StringUtils.join(cronList,";");
    }*/

}
