package com.simatelex.admin.util;

import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Nancy Zhao
 * @Date 2026/06/05 16:05
 * @Version 1.0
 **/
public class IpUtil {
    /**
     * 获取本机所有有效内网IP（排除127.0.0.1、回环、虚拟网卡）
     */
    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                // 跳过未启用、回环、虚拟网卡
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    // 只取IPv4，排除IPV6、本地回环
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    /**
     * 获取本机电脑名
     * @return
     */
    public static String getHostName() {
        String hostName = null;
        // 优先系统变量
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            hostName = System.getProperty("COMPUTERNAME");
        }else{
            hostName = System.getenv("HOSTNAME");
        }
        // 系统变量为空再用InetAddress
        if(hostName == null || hostName.isBlank()){
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                hostName = "unknown";
            }
        }
        return hostName;
    }

    public static HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null){
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * 获取IP地址 使用 Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非
     * unknown的有效IP字符串，则为真实IP地址
     */
    public static String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // log.info("获取客户端ip: " + ip);
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    public static Map<String,String> setBrowser(HttpServletRequest request) {
        Map<String, String> mapUserAgent = new HashMap<String, String>();
        // 先判空，兼容定时任务/异步线程
        if (request == null) {
            // 没有请求上下文时，填充默认值
            mapUserAgent.put("clientType", "定时任务/邮箱定时任务");
            mapUserAgent.put("os", "定时任务/邮箱定时任务");
            mapUserAgent.put("ip", "定时任务/邮箱定时任务");
            mapUserAgent.put("method", "定时任务/邮箱定时任务");
            mapUserAgent.put("browser", "定时任务/邮箱定时任务");
            mapUserAgent.put("serverName", getHostName());
            return mapUserAgent;
        }
        try {
            UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("user-agent"));
            String clientType = userAgent.getOperatingSystem().getDeviceType().toString();
            String os = userAgent.getOperatingSystem().getName();
            String ip = IpUtil.getClientIpAddr(request);
            String method = request.getMethod();
            String browser = userAgent.getBrowser().toString();
            String serverName = InetAddress.getLocalHost().getHostName();
            //从浏览器获取的ip 如：127.0.0.1
//            System.out.println("------> IP ADDR: " + ip);    //IP address.
            //从电脑获取的ip 如：本机191.191.23.69
//            String hostAddress = InetAddress.getLocalHost().getHostAddress();
//            System.out.println("------> IP: " + hostAddress);
            //设备类型，如：COMPUTER
            mapUserAgent.put("clientType", clientType);
            //系统版本名 如：Windows 10
            mapUserAgent.put("os", os);
            //ip
            mapUserAgent.put("ip",ip);
            //请求参数 如：GET,POST
            mapUserAgent.put("method", method);
            //浏览器类型 如：CHROME10
            mapUserAgent.put("browser", browser);
            //系统电脑名
            mapUserAgent.put("serverName", serverName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            mapUserAgent.put("browser","unknown");
        }
        return mapUserAgent;
    }
}
