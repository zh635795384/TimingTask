package com.simatelex.admin.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author Nancy Zhao
 * @Date 2025/08/15 17:00
 * @Version 1.0
 **/
public class GetPCName {

    public static String getComputerName(){
        InetAddress localHost = null;
        String computerName = null;
        try {
            localHost = InetAddress.getLocalHost();
            // 获取主机名
            computerName = localHost.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // 返回主机名
        return computerName;
    }
}
