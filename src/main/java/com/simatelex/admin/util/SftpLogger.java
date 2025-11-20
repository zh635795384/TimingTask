package com.simatelex.admin.util;

import com.jcraft.jsch.Logger;

import java.util.Hashtable;

/**
 * @Author Nancy Zhao
 * @Date 2024/01/18 15:58
 * @Version 1.0
 **/
public class SftpLogger implements Logger {

    static Hashtable<Integer, String> name = new Hashtable<Integer, String>();
    static {
        name.put(new Integer(DEBUG), "DEBUG: ");
        name.put(new Integer(INFO), "INFO: ");
        name.put(new Integer(WARN), "WARN: ");
        name.put(new Integer(ERROR), "ERROR: ");
        name.put(new Integer(FATAL), "FATAL: ");
    }
    @Override
    public boolean isEnabled(int level) {
        return true;
    }

    @Override
    public void log(int level, String message) {
        //这里我们就用控制台红色字体输出
        System.err.println(name.get(new Integer(level)));
        System.err.println(message);
    }
}
