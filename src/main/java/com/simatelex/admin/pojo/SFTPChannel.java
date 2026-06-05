package com.simatelex.admin.pojo;

import com.jcraft.jsch.*;
import com.simatelex.admin.util.SftpLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Properties;

/**
 * @Author Nancy Zhao
 * @Date 2024/01/10 15:47
 * @Version 1.0
 **/
@Slf4j
public class SFTPChannel {
    Session session = null;
    Channel channel = null;


//    private static final Logger LOG = log.error(SFtpUtils.class.getName());

    public ChannelSftp getChannel(Map<String, String> sftpDetails, int timeout) throws JSchException {
        String ftpHost = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
        String port = sftpDetails.get(SFTPConstants.SFTP_REQ_PORT);
        String ftpUserName = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
        String ftpPassword = sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD);

        int ftpPort = SFTPConstants.SFTP_DEFAULT_PORT;
        if (port != null && !port.equals("")) {
            ftpPort = Integer.valueOf(port);
        }
        JSch.setLogger(new SftpLogger());//设置打印类
        JSch jsch = new JSch(); // 创建JSch对象
        session = jsch.getSession(ftpUserName, ftpHost, ftpPort); // 根据用户名，主机ip，端口获取一个Session对象
        System.setProperty("http.proxyType", "4");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("http.proxyHost", "191.191.23.27");
        System.setProperty("http.proxySet", "true");
        System.setProperty("ftpProxyPort", "8080");
        System.setProperty("ftpProxyHost","191.191.23.27");
        System.setProperty("ftpProxySet", "true");
//        session.setProxy(new ProxyHTTP("191.191.23.27", 8080));
        log.debug("Session created.");
        if (StringUtils.isNotEmpty(ftpPassword)) {
            session.setPassword(ftpPassword); // 设置密码
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config); // 为Session对象设置properties
        session.setTimeout(timeout); // 设置timeout时间
        session.connect(); // 通过Session建立链接
        log.debug("Session connected.");

        log.debug("Opening Channel.");
        channel = session.openChannel("sftp"); // 打开SFTP通道
        channel.connect(); // 建立SFTP通道的连接
        log.debug("Connected successfully to ftpHost = " + ftpHost + ",as ftpUserName = " + ftpUserName
                + ", returning: " + channel);
        return (ChannelSftp) channel;
    }

    public void closeChannel() throws Exception {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }
}
