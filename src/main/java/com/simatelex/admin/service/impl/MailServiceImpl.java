package com.simatelex.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.ChannelSftp;
import com.simatelex.admin.config.mail.SetCron;
import com.simatelex.admin.mappers.xfbps01.MailTablesMapper;
import com.simatelex.admin.mappers.xfstd01.MailMapper;
import com.simatelex.admin.mappers.xfstd01.MailReceiverMapper;
import com.simatelex.admin.pojo.*;
import com.simatelex.admin.query.MailQuery;
import com.simatelex.admin.service.IMailService;
import com.simatelex.admin.service.ISystemLogService;
import com.simatelex.admin.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Nancy Zhao
 * @Date 2025/09/29 16:39
 * @Version 1.0
 **/
@Service
@Slf4j
public class MailServiceImpl extends ServiceImpl<MailMapper, Mail> implements IMailService {

    @Autowired
    JavaMailSender mailSender;

    @Resource
    ISystemLogService systemLogService;

    @Autowired
    EmailScheduleServiceImpl emailScheduleService;

    @Resource
    MailTablesMapper mailTablesMapper;

    @Resource
    MailReceiverMapper mailReceiverMapper;

    public SFTPChannel getSFTPChannel(){
        return new SFTPChannel();
    }

    @Override
    public Map<String, Object> mailList(MailQuery mailQuery) {
        //分页，两个参数，一个当前页，一个条数
        IPage<Mail> page = new Page<>(mailQuery.getPage(), mailQuery.getLimit());
        //开启多条件查询
        QueryWrapper<Mail> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("isnull(is_del,0)", 0);//判断是否已经删除
        //将查询结果返回给Page
        page = this.baseMapper.selectPage(page, queryWrapper);
        return PageResultUtil.getResult(page.getTotal(), page.getRecords());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, transactionManager = "xfstd01Manager")   //事务控制
    public void saveMail(Mail mail) {
        validateMail(mail);
        mail.setIsDel(false);
        AssertUtil.isTrue(!this.save(mail), "添加失败！");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, transactionManager = "xfstd01Manager")   //事务控制
    public void updateMail(Mail mail, boolean isRun) {
        validateMail(mail);
        AssertUtil.isTrue(!this.updateById(mail), "更新失败");
        if (isRun) {
            emailScheduleService.modifyJob(mail.getTableName(), "group_" + mail.getTableName(), SetCron.getCron(mail), mail);
        }
    }

    /** 新增/更新共用校验（与 SetCron 一致：类型;时间） */
    private void validateMail(Mail mail) {
        AssertUtil.isTrue(mail == null, "请输入数据");
        AssertUtil.isTrue(StringUtils.isBlank(mail.getTableName()), "表名不能为空");
        AssertUtil.isTrue(mail.getTableName().split("-").length > 2, "符号-不能超过1个");
        AssertUtil.isTrue(Boolean.TRUE.equals(mail.getDivExcel()) && StringUtils.isBlank(mail.getGroupBy()), "开启分sheet时必须填写分组名称");
        // sendTime 为空时 SetCron 默认每 10 分钟执行，不强制填写
        String sendTarget = mail.getSendTarget();
        String sendTime = mail.getSendTime();
        if (StringUtils.isBlank(sendTime)) {
            return;
        }
        if ("每星期".equals(sendTarget) || "每周".equals(sendTarget)
                || "每月".equals(sendTarget) || "每年".equals(sendTarget)) {
            String[] parts = sendTime.split(";");
            AssertUtil.isTrue(parts.length < 2
                            || StringUtils.isBlank(parts[0].trim())
                            || StringUtils.isBlank(parts[1].trim())
                            || !parts[1].contains(":"),
                    "发送时间格式错误，应为：类型;时间，如 1;08:00:00");
        } else if ("每天".equals(sendTarget)) {
            String timePart = sendTime.contains(";") ? sendTime.substring(sendTime.indexOf(';') + 1) : sendTime;
            AssertUtil.isTrue(!timePart.contains(":"), "每天发送时间格式错误，应为 hh:mm:ss");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, transactionManager = "xfstd01Manager")   //事务控制
    public void deleteMail(Integer[] ids, Integer flag) {
        AssertUtil.isTrue(ids == null || ids.length == 0, "请正确选择数据");
        List<Mail> mailList = new ArrayList<>();
        for (Integer id : ids) {
            Mail mail = this.getById(id);
            mail.setIsDel(flag.equals(1));
            mailList.add(mail);
            systemLogService.addToLog(mail, flag.equals(1) ? "邮箱信息"+mail.getTableName()+"删除成功" : "邮箱"+mail.getTableName()+"恢复成功");
        }
        AssertUtil.isTrue(!this.updateBatchById(mailList), "数据更新失败");
    }

    @Override
    public void sendMail(Mail mail) {
        try {
            // 合并子报表（contact=NO，或被其他报表的 contact 引用）不单独发信，由主报表合并发送
            if (isMergeChildOnly(mail)) {
                systemLogService.addToLog(mail, mail.getTableName() + "为合并子报表，跳过独立发送（由主报表合并附件）");
                return;
            }
            //如果是ftp上传
            if (Boolean.TRUE.equals(mail.getIsFtpUpload())){
                String customDate = DateUtil.formatTableStyleDate(mail.getTableStyle());
                String fileStem = (StringUtils.isNotBlank(mail.getTablePrefix()) ? mail.getTablePrefix() : mail.getTableName()) + customDate + "." + mail.getTableSuffix();
                String localPath = joinPath(mail.getFtpLocalPath(), fileStem);
                if (StringUtils.isNotBlank(mail.getSubject())) {
                    //根据字段查需要检测的报表
                    Mail checkMail = this.baseMapper.selectOne(new QueryWrapper<Mail>().eq("table_name", mail.getSubject()));
                    if (checkMail != null) {
                        String checkDate = DateUtil.formatTableStyleDate(checkMail.getTableStyle());
                        String checkStem = (StringUtils.isNotBlank(checkMail.getTablePrefix()) ? checkMail.getTablePrefix() : checkMail.getTableName()) + checkDate + "." + checkMail.getTableSuffix();
                        String checkPath = joinPath(checkMail.getFtpLocalPath(), checkStem);
                        if (StringUtils.isNotBlank(checkPath)) {
                            if (!isFileExist(checkPath)) {
                                systemLogService.addToLog(mail,"ftp-"+mail.getTableName()+"上传失败！原因："+mail.getSubject()+ "不存在！");
                                return;
                            }
                        }
                    }
                }
                //上传一个文件
                try{
//                FileInputStream in = new FileInputStream(new File(localPath));
//                boolean test = FtpUtil.uploadFile(mail.getFtpServer(), mail.getFtpUserId(), mail.getFtpUserPwd(), mail.getFtpPort(), mail.getPath(), fileName,in);
//                boolean test = SFtpUtils.downLoadFileBySsh(mail.getFtpServer(), mail.getFtpPort(), mail.getFtpUserId(), mail.getFtpUserPwd(), mail.getFtpLocalPath(), mail.getPath());
                    Map<String, String> sftpDetails = new HashMap<String, String>();
                    sftpDetails.put(SFTPConstants.SFTP_REQ_HOST, mail.getFtpServer());
                    sftpDetails.put(SFTPConstants.SFTP_REQ_USERNAME, mail.getFtpUserId());
                    sftpDetails.put(SFTPConstants.SFTP_REQ_PASSWORD, mail.getFtpUserPwd());
                    sftpDetails.put(SFTPConstants.SFTP_REQ_PORT, String.valueOf(mail.getFtpPort()));
                    SFTPChannel channel = this.getSFTPChannel();
                    ChannelSftp chSftp = channel.getChannel(sftpDetails, 60000);
//                chSftp.put(mail.getFtpLocalPath(), mail.getPath(), ChannelSftp.OVERWRITE);
                    if (isFileExist(localPath)) {
                        //put第一个参数为文件完整路径，包括文件全称
                        chSftp.put(localPath, mail.getPath(), ChannelSftp.OVERWRITE);
                        systemLogService.addToLog(mail,"ftp-"+mail.getTableName()+"上传成功！");
                    }
                    chSftp.quit();
                    channel.closeChannel();
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                    System.out.println(e);
                    log.error(String.valueOf(e));
                    systemLogService.addToLog(mail,"ftp-"+mail.getTableName()+"上传失败！原因：本地路径不存在");
                } catch (Exception e) {
                    systemLogService.addToLog(mail,"ftp-"+mail.getTableName()+"上传失败！原因："+ (e.toString().length()>=8000 ? e.toString().substring(0,7550) : e.toString()));
                    e.printStackTrace();
                }

            /*//在FTP服务器上生成一个文件，并将一个字符串写入到该文件中
            try {
                InputStream input = new ByteArrayInputStream("test ftp jyf".getBytes("GBK"));
                boolean flag = FtpUtil.uploadFile(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpPath, fileName,input);;
                System.out.println(flag);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //下载一个文件
            FtpUtil.downloadFtpFile(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpPath, localPath, fileName);*/
            } else {
                //如果只导出：有有效导出文件才清表
                if (Boolean.TRUE.equals(mail.getIsExport())) {
                    List<MailReceiver> exported = exportExcelPath(mail);
                    if (hasValidExportFiles(exported)) {
                        this.truncateTable(mail);
                    } else {
                        systemLogService.addToLog(mail, mail.getTableName() + "导出无有效文件，跳过清表");
                    }
                } else {
                    // 主报表按收件人/分组导出（一封邮件可带多个附件）
                    List<MailReceiver> mailGroupList = exportExcelPath(mail);
                    List<String> tableNameList = new ArrayList<>();
                    // 关联报表导出结果（带 groupKey，按分组值合并到对应邮件）
                    List<MailReceiver> mergedExtras = new ArrayList<>();
                    if (hasValidExportFiles(mailGroupList)) {
                        tableNameList.add(mail.getTableName());
                    } else {
                        mailGroupList = new ArrayList<>();
                    }
                    // 关联报表只导出文件，合并到主邮件附件中（使用主报表收件人；有 groupBy 时按分组值对齐）
                    if (StringUtils.isNotBlank(mail.getContact()) && !"NO".equalsIgnoreCase(mail.getContact().trim())) {
                        for (String contact : mail.getContact().split(";")) {
                            contact = contact == null ? "" : contact.trim();
                            if (StringUtils.isBlank(contact) || "NO".equalsIgnoreCase(contact)) {
                                continue;
                            }
                            Mail mailContact = this.baseMapper.selectOne(new QueryWrapper<Mail>().eq("table_name", contact).eq("isnull(is_del,0)",0));
                            if (mailContact == null) {
                                log.warn("关联报表[{}]不存在或已删除，已跳过", contact);
                                continue;
                            }
                            List<MailReceiver> contactPathList = exportExcelPath(mailContact);
                            if (hasValidExportFiles(contactPathList)) {
                                tableNameList.add(contact);
                                for (MailReceiver c : contactPathList) {
                                    if (c != null && StringUtils.isNotBlank(c.getPath()) && new File(c.getPath()).exists()) {
                                        mergedExtras.add(c);
                                    }
                                }
                            }
                        }
                    }
                    // 主报表无文件但有关联附件时，用收件人配置发一封合并邮件
                    if (mailGroupList.isEmpty() && !mergedExtras.isEmpty()) {
                        List<MailReceiver> receivers = this.selectGroupBy(mail.getId());
                        if (receivers == null || receivers.isEmpty()) {
                            MailReceiver fallback = new MailReceiver();
                            fallback.setReceiver(null);
                            receivers = Collections.singletonList(fallback);
                        }
                        for (MailReceiver receiver : receivers) {
                            MailReceiver group = new MailReceiver();
                            group.setReceiver(receiver.getReceiver());
                            group.setCc(receiver.getCc());
                            group.setBcc(receiver.getBcc());
                            group.setGroupKey(receiver.getGroupKey());
                            group.setPath(null);
                            group.setTableName(mail.getTableName());
                            mailGroupList.add(group);
                        }
                    }
                    int successCount = 0;
                    for (MailReceiver mailGroup : mailGroupList) {
                        MimeMessage mimeMessage = mailSender.createMimeMessage();
                        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                        String path = mailGroup.getPath();
                        String fileName = extractFileBaseName(path);
                        if (StringUtils.isBlank(fileName)) {
                            fileName = StringUtils.isNotBlank(mail.getTablePrefix()) ? mail.getTablePrefix() : mail.getTableName();
                        }
                        helper.setSubject((mail.getSubject() == null ? "" : mail.getSubject()) + "_" + fileName);
                        AssertUtil.isTrue(StringUtils.isBlank(mailGroup.getReceiver()), "收件者为空，无法发送");
                        helper.setTo(mailGroup.getReceiver().trim().replaceAll(";+$", "").split(";"));
                        helper.setFrom(mail.getSender());
                        if (StringUtils.isNotBlank(mailGroup.getCc())) {
                            helper.setCc(mailGroup.getCc().trim().replaceAll(";+$", "").split(";"));
                        }
                        if (StringUtils.isNotBlank(mailGroup.getBcc())) {
                            helper.setBcc(mailGroup.getBcc().trim().replaceAll(";+$", "").split(";"));
                        }
                        String body = mail.getBody() == null ? "" : mail.getBody().replace("{subject}", mail.getSubject() == null ? "" : mail.getSubject());
                        body = body.replaceAll("\\r?\\n", "\r\n");
                        helper.setText(body);

                        if (Boolean.TRUE.equals(mail.getEmptyAttach())) {
                            mailSender.send(mimeMessage);
                            successCount++;
                            systemLogService.addToLog(mail, fileName + "发送成功！");
                        } else {
                            List<String> attachPaths = new ArrayList<>();
                            if (StringUtils.isNotBlank(path) && new File(path).exists()) {
                                attachPaths.add(path);
                            }
                            attachPaths.addAll(matchExtrasByGroupKey(mailGroup, mergedExtras));
                            AssertUtil.isTrue(attachPaths.isEmpty(), "没有可发送的附件");
                            for (String attachPath : attachPaths) {
                                String readyPath = prepareAttachPath(mail, attachPath);
                                FileDataSource dataSource = new FileDataSource(readyPath);
                                // 附件名使用实际文件名，保留别名/分组信息
                                helper.addAttachment(dataSource.getName(), dataSource);
                            }
                            mailSender.send(mimeMessage);
                            systemLogService.addToLog(mail, fileName + "发送成功！");
                            successCount++;
                        }
                    }
                    // 全部发送成功后再清表，避免中途失败导致无法重试
                    if (successCount > 0 && successCount == mailGroupList.size() && !tableNameList.isEmpty()) {
                        truncateTable(mail, tableNameList);
                    } else if (!mailGroupList.isEmpty() && successCount < mailGroupList.size()) {
                        systemLogService.addToLog(mail, "部分邮件未发送成功，跳过清表");
                    }
                }
            }
        } catch (Exception e) {
            String msg = Optional.ofNullable(e.getMessage()).orElse("未知异常信息");
            systemLogService.addToLog(mail,"邮件发送异常！原因："+ (msg.length() > 4970 ? msg.substring(0, 4970) : msg));
            e.printStackTrace();
        }
    }

    /**
     * 第一种方式：批量清理
     * @param mail
     * @param tableNameList
     */
    private void truncateTable(Mail mail, List<String> tableNameList) {
        for (String truncateTable : tableNameList) {
            assertSafeSqlIdentifier(truncateTable, "清表名");
            mailTablesMapper.truncateTable(truncateTable);
            systemLogService.addToLog(mail, truncateTable + "清理成功！");
        }
    }

    /**
     * 第二种方式：单独清理
     * @param mail
     */
    private void truncateTable(Mail mail) {
            assertSafeSqlIdentifier(mail.getTableName(), "清表名");
            mailTablesMapper.truncateTable(mail.getTableName());
            systemLogService.addToLog(mail, mail.getTableName() + "清理成功！");
    }

    @Override
    public void runEmail(boolean run){
        if (run){
            try {
                try {
                    //先彻底关闭再重启，清空历史状态
                    emailScheduleService.standby();
                    //短暂等待释放资源
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // 1. 先唤醒/启动调度器（解决待机不执行问题）
                emailScheduleService.startScheduler();
                log.info("调度器已唤醒/启动");
                List<Mail> mailList = this.baseMapper.selectList(new QueryWrapper<Mail>().eq("is_del", 0));
                for (Mail mail : mailList) {
                    String cron = SetCron.getCron(mail);
                    try {
                        String jobName = mail.getTableName();
                        String jobGroup = "group_"+mail.getTableName();
                        //重新加载任务（会自动删除旧任务、重建触发器）
                        emailScheduleService.scheduleEmailJob(mail.getTableName(), jobGroup, mail,cron);
                        log.info("任务【{}】加载完成", jobName);
                    } catch (SchedulerException e) {
                        log.error("加载定时任务异常", e);
                        e.printStackTrace();
                    }
                }
            } catch (SchedulerException e) {
                log.error("启动调度器失败",e);
            }
        } else {
            // 2. 停止：改用彻底关闭，不再用 standby（根治状态残留）
            emailScheduleService.standby();
            log.info("调度器已彻底停止");
        }
    }

    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 导出成EXCEL
     * 1.判断是不是分table，groupBy字段不为空
     * 2.判断是不是分sheet，divExcel为true
     * 如果需要分组发送的话，groupBy字段不为空并且收件者表有数据，但是如果需要分sheet不分组发送的话，就divExcel为true
     * @param mail
     * @return
     */
    public List<MailReceiver> exportExcelPath(Mail mail){
        //返回的实体
        List<MailReceiver> mailReceiverList = new ArrayList<>();
        if (mail == null) {
            log.warn("导出失败：邮件配置为空");
            return mailReceiverList;
        }
        assertSafeSqlIdentifier(mail.getTableName(), "表名");
        if (StringUtils.isNotBlank(mail.getGroupBy())) {
            assertSafeSqlIdentifier(mail.getGroupBy().replaceAll("^\\[|\\]$", ""), "分组字段");
        }
        //查询条件
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        //sheet个数
        int sheetCount = 1;
        //工作表个数
        int workbookCount = 1;
        //  源路径
        String sourcePath = null;
        //表名
        String tableName = mail.getTableName();
        //分组收件人(分table场景使用)
        List<MailReceiver> groupByList = this.selectGroupBy(mail.getId());
        AssertUtil.isTrue(StringUtils.isBlank(mail.getGroupBy()) && groupByList.size() > 1, "不是分组发送，只能保留一行收件者配置，请修改！");
        //分组分sheet
        List<Map<String, Object>> divExcelMapList = new ArrayList<>();
        if (StringUtils.isNotBlank(mail.getGroupBy())) {
            divExcelMapList = this.selectDivExcel(mail.getTableName(), mail.getGroupBy());
        }
        //场景一：只有在收件者表不为空，并且分组字段不为空，并且不分sheet的时候才分table，即使查出来实际table中的分组数>收件者列表的个数，依然取收件者列表中的个数
        if (!groupByList.isEmpty() && StringUtils.isNotBlank(mail.getGroupBy()) && !Boolean.TRUE.equals(mail.getDivExcel())) {
            //设置工作表个数
            workbookCount = groupByList.size();
        }
        //场景二：如果根据group by分sheet
        if (Boolean.TRUE.equals(mail.getDivExcel()) && !divExcelMapList.isEmpty()){
            //如果要分sheet，就只能是一个工作表
            workbookCount = 1;
            //分组之后的sheet个数
            sheetCount = divExcelMapList.size();
        }
        String customDate = DateUtil.formatTableStyleDate(mail.getTableStyle());
        //如果有自定义的字段
        String column = null;
        if (StringUtils.isNotBlank(mail.getOutColumn())) {
            column = mail.getOutColumn();
        } else {
            column ="*";
        }
        // ========== 新增：构建 原字段名 -> 最终别名Key 映射 ==========
        LinkedHashMap<String, String> fieldAliasMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(mail.getOutColumn())) {
            // 按逗号拆列时忽略 [] 内的逗号；兼容 AS 别名 与 [字段] [别名]
            for (String expr : splitOutColumns(mail.getOutColumn())) {
                String[] fieldAlias = parseOutColumnExpr(expr);
                if (fieldAlias != null) {
                    fieldAliasMap.put(fieldAlias[0], fieldAlias[1]);
                }
            }
        }
        String aliasTitleOld = String.join(",", fieldAliasMap.keySet());
        String aliasTitle = aliasTitleOld;
        if (StringUtils.isNotBlank(mail.getGroupBy())
                && !aliasTitleOld.toLowerCase().contains(mail.getGroupBy().toLowerCase())) {
            aliasTitle = aliasTitleOld + "," + mail.getGroupBy();
        }
        String oldTitle = !"*".equals(column) ? aliasTitle : "*";
        queryWrapper.select(oldTitle);
        if (StringUtils.isNotBlank(mail.getOrderBy())) {
            String[] orderByList = mail.getOrderBy().split(",");
            for (String orderBy : orderByList) {
                orderBy = orderBy == null ? "" : orderBy.trim();
                if (StringUtils.isBlank(orderBy)) {
                    continue;
                }
                String[] s = orderBy.split("\\s+");
                String orderColumn = s[0].trim();
                assertSafeSqlIdentifier(orderColumn.replaceAll("^\\[|\\]$", ""), "排序字段");
                if (s.length >= 2 && "desc".equalsIgnoreCase(s[1])) {
                    queryWrapper.orderByDesc(orderColumn);
                } else {
                    queryWrapper.orderByAsc(orderColumn);
                }
            }
        }
        //查询xfbps01中的表数据   select * from ${tableName} where ${groupBy}=值
        List<LinkedHashMap<String, Object>> tableList = this.selectTables(mail.getTableName(), queryWrapper);
        if (tableList.isEmpty()) {
            // 无数据直接返回
            return mailReceiverList;
        }
        List<String> excelTitles = new ArrayList<>();
        List<String> dataColumns = new ArrayList<>();
        if ("*".equals(column)) {
            if (!tableList.isEmpty() && tableList.get(0) != null) {
                excelTitles.addAll(tableList.get(0).keySet());
                dataColumns.addAll(tableList.get(0).keySet());
                aliasTitleOld = String.join(",", dataColumns);
            }
        } else {
            excelTitles.addAll(fieldAliasMap.values());
            dataColumns.addAll(fieldAliasMap.keySet());
        }
        /*titleWrapper.select(newTitle);
        //为了保证标题行不变
        List<LinkedHashMap<String, Object>> titleList = this.selectTables(mail.getTableName(), titleWrapper);*/
        /*//将表中所有字段转小写
        tableList = tableList.stream()
                .map(row -> {
                    LinkedHashMap<String, Object> lowerRow = new LinkedHashMap<>();
                    for (Map.Entry<String, Object> entry : row.entrySet()) {
                        lowerRow.put(entry.getKey().toLowerCase(), entry.getValue());
                    }
                    return lowerRow;
                })
                .collect(Collectors.toList());*/
        String groupField = StringUtils.isBlank(mail.getGroupBy()) ? "" : mail.getGroupBy().trim();
        //要有数据
        if (tableList.size() > 0) {
            //循环工作表
            for (int i = 0; i < workbookCount; i++) {
                //分组实体
                MailReceiver mailGroup = new MailReceiver();
                //将分组拿出来
                MailReceiver mailReceiver = groupByList.size() > i ? groupByList.get(i) : new MailReceiver();
                //用来装分组之后的数据
                List<LinkedHashMap<String, Object>> targetData = new ArrayList<>();
                //场景一：分文件(多workbook)，按收件人分组过滤数据
                String fileBaseName;
                if (workbookCount > 1 && StringUtils.isNotBlank(mail.getGroupBy()) && !Boolean.TRUE.equals(mail.getDivExcel())) {
                    String groupKey = mailReceiver.getGroupKey() == null ? "" : mailReceiver.getGroupKey().trim();
                    targetData = tableList.stream().filter(row -> groupKey.equalsIgnoreCase(StringUtil.getValueIgnoreCase(row, groupField))).collect(Collectors.toList());
//                    targetData = tableList.stream().filter(row -> groupKey.equalsIgnoreCase((String) row.get(groupField))).collect(Collectors.toList());
                    //如果有分组并且要分sheet就直接取表名和分组值
                    tableName = mail.getTableName() + "_" + groupKey;
                    // 文件名优先用别名+分组
                    String namePrefix = StringUtils.isNotBlank(mail.getTablePrefix()) ? mail.getTablePrefix() : mail.getTableName();
                    fileBaseName = namePrefix + "_" + groupKey;
                } else {
                    //场景二：单个文件、多sheet，外层先赋值全量，内层sheet循环再细分
//                    targetData = titleList;
                    targetData = tableList;
                    tableName = mail.getTableName();
                    // 有别名则：别名+table_style；否则：表名+table_style
                    fileBaseName = StringUtils.isNotBlank(mail.getTablePrefix()) ? mail.getTablePrefix() : mail.getTableName();
                }
                //如果有数据才需要创建，否则不需要创建workbook
                if (targetData.size() > 0) {
                    //创建工作簿
                    SXSSFWorkbook workbook = new SXSSFWorkbook();
                    //循环,单个Excel内多个sheet
                    for (int j = 0; j < sheetCount; j++) {
                        //将分sheet数据拿出来
                        Map<String, Object> divExcelMap = divExcelMapList.size() > 0 ? divExcelMapList.get(j) : new HashMap<>();
                        //每个sheet单独过滤数据
                        List<LinkedHashMap<String, Object>> sheetData = new ArrayList<>();
                        // 当前Sheet对应的分组值
                        String sheetGroupVal = divExcelMap.get(mail.getGroupBy()) == null ? ""
                                : String.valueOf(divExcelMap.get(mail.getGroupBy())).trim();//.toLowerCase();
                        //如果groupBy没有值就直接用全量数据；如果groupBy有值并且divExcel=false就是要分table的，就取targetData；如果groupBy有值并且divExcel=true就是要分sheet的，就取sheetData
                        if (sheetCount > 1 && StringUtils.isNotBlank(mail.getGroupBy()) && Boolean.TRUE.equals(mail.getDivExcel())) {
                            /*List<LinkedHashMap<String, Object>> newSheetTitle = new ArrayList<>();
                            for (int idx = 0; idx < tableList.size(); idx++) {
                                LinkedHashMap<String, Object> lowerRow = originLowerCaseTableList.get(idx);
                                Object valObj = lowerRow.get(actualMapKeyLower);
                                if (valObj == null) {continue;}
                                String valStr = String.valueOf(valObj).trim();
                                if (sheetGroupVal.equalsIgnoreCase(valStr)) {
                                    newSheetTitle.add(targetData.get(idx));
                                }
                            }
                            sheetData = newSheetTitle;*/
                            sheetData = targetData.stream().filter(row -> sheetGroupVal.equalsIgnoreCase(StringUtil.getValueIgnoreCase(row, groupField))).collect(Collectors.toList());
//                            sheetData = targetData.stream().filter(row -> sheetGroupVal.equalsIgnoreCase(String.valueOf(row.get(groupField)).trim())).collect(Collectors.toList());
                        } else if (sheetCount == 1 && workbookCount == 1){
                            sheetData = tableList;
                        } else if (workbookCount > 1 && StringUtils.isNotBlank(mail.getGroupBy()) && !Boolean.TRUE.equals(mail.getDivExcel())) {
                            sheetData = targetData;
                        }
                        //工作表
                        SXSSFSheet sheet = workbook.createSheet();
                        //设置sheet表名，如果只有一个sheet就是报表名，否则就是分组值
                        Object groupVal = null;
                        if (sheetCount > 1 && StringUtils.isNotBlank(mail.getGroupBy())) {
                            groupVal = divExcelMap.get(mail.getGroupBy());
                            if (groupVal == null) {
                                for (Map.Entry<String, Object> e : divExcelMap.entrySet()) {
                                    if (e.getKey() != null && e.getKey().equalsIgnoreCase(mail.getGroupBy())) {
                                        groupVal = e.getValue();
                                        break;
                                    }
                                }
                            }
                        }
                        workbook.setSheetName(j, sanitizeSheetName(sheetCount == 1 ? tableName : String.valueOf(groupVal), j));
                        //设置首行加粗字体
                        CellStyle cellStyle1 = ExcelStyleUtil.getCellStyle2(workbook, ExcelStyleUtil.getXssfFont15(workbook, StringUtils.isNotBlank(mail.getFonts()) ? mail.getFonts() : "Times New Roman", mail.getFontSize() != null ? mail.getFontSize() : 10));
                        //设置其他行不加粗
                        CellStyle cellStyle2 = ExcelStyleUtil.getCellStyle2(workbook, ExcelStyleUtil.getXssfFont16(workbook, StringUtils.isNotBlank(mail.getFonts()) ? mail.getFonts() : "Times New Roman", mail.getFontSize() != null ? mail.getFontSize() : 9));
                        //设置标题行
                        setRowTitle(excelTitles, sheet, cellStyle1);
//                        setRow1(mail, titleList, sheet, cellStyle1);
                        //设置数据行
                        setOtherRow(sheet, cellStyle2, dataColumns, sheetData, Boolean.TRUE.equals(mail.getInsertRow()));
                    }
                    //输出到模板中
                    BufferedOutputStream out = null;
                    try {
                        // 生成源文件路径（文件名：别名优先，否则表名）+ table_style
                        String parentDirPath = new File(System.getProperty("user.dir")).getParent()
                                + "\\doc\\" + mail.getTableName();
                        //格式：G:\Program\doc\test\XF202609.xlsx
                        sourcePath = parentDirPath + "\\" + fileBaseName + customDate + ".xlsx";
                        File sourceFile = new File(sourcePath);
                        //获取上级目录
//                        boolean dr = sourceFile.getParentFile().mkdirs();
                        // 强制创建所有不存在的父目录（关键！解决找不到路径问题）
                        File parentDir = sourceFile.getParentFile();
                        if (!parentDir.exists()) {
                            parentDir.mkdirs();
                            log.info("自动创建目录：{}", parentDir.getAbsolutePath());
                        }
                        //如果此文件存在则先删除
                        if (sourceFile.exists()) {
                            sourceFile.delete();
                        }
                        //以以上路径新建输出流
                        out = new BufferedOutputStream(new FileOutputStream(sourcePath));
                        //将内容写到输出流中
                        workbook.write(out);
                        //flush是把流里的缓冲数据输出，flush以后还能继续使用这个OutputStream
                        out.flush();
                        String attachPath = sourcePath;
                        // 开启压缩：本地与导出路径都生成 zip，邮件附件也走这个路径
                        if (Boolean.TRUE.equals(mail.getIsRar())) {
                            String zipFileName = fileBaseName + customDate + ".zip";
                            File zipLocal = new File(parentDirPath, zipFileName);
                            try (FileOutputStream zipOut = new FileOutputStream(zipLocal)) {
                                ZipUtils.toZip(sourcePath, zipOut, true);
                            }
                            attachPath = zipLocal.getAbsolutePath();
                            // 压缩成功后删除本地 xlsx，避免重复占用
                            if (sourceFile.exists()) {
                                sourceFile.delete();
                            }
                        }
                        if (StringUtils.isNotBlank(mail.getPath())) {
                            File targetDir = new File(mail.getPath());
                            if (!targetDir.exists()) {
                                targetDir.mkdirs();
                            }
                            if (Boolean.TRUE.equals(mail.getIsRar())) {
                                String zipFileName = fileBaseName + customDate + ".zip";
                                String targetZipPath = joinPath(mail.getPath(), zipFileName);
                                Files.copy(Paths.get(attachPath), Paths.get(targetZipPath), StandardCopyOption.REPLACE_EXISTING);
                                File oldXlsx = new File(joinPath(mail.getPath(), fileBaseName + customDate + ".xlsx"));
                                if (oldXlsx.exists()) {
                                    oldXlsx.delete();
                                }
                                // 清理旧的“表名+日期”命名文件，避免残留
                                File legacyXlsx = new File(joinPath(mail.getPath(), mail.getTableName() + customDate + ".xlsx"));
                                if (legacyXlsx.exists()) {
                                    legacyXlsx.delete();
                                }
                                File legacyZip = new File(joinPath(mail.getPath(), mail.getTableName() + customDate + ".zip"));
                                if (legacyZip.exists()) {
                                    legacyZip.delete();
                                }
                                log.info("压缩导出完成：{}", targetZipPath);
                            } else {
                                String targetPath = joinPath(mail.getPath(), fileBaseName + customDate + ".xlsx");
                                Files.copy(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                        //添加路径到返回路径中
//                        pathList.add(sourcePath);
                        mailGroup.setTableName(tableName);
                        mailGroup.setReceiver(mailReceiver.getReceiver());
                        mailGroup.setPath(attachPath);
                        // 分文件发送时带上分组值，供关联报表按 groupBy 合并
                        if (workbookCount > 1 && StringUtils.isNotBlank(mail.getGroupBy()) && !Boolean.TRUE.equals(mail.getDivExcel())) {
                            mailGroup.setGroupKey(mailReceiver.getGroupKey() == null ? "" : mailReceiver.getGroupKey().trim());
                        } else {
                            mailGroup.setGroupKey(mailReceiver.getGroupKey());
                        }
                        mailGroup.setCc(mailReceiver.getCc());
                        mailGroup.setBcc(mailReceiver.getBcc());
                        mailReceiverList.add(mailGroup);
                        systemLogService.addToLog(tableName,"导出成功！");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally { //释放资源
                        try {
                            if (out != null) {
                                //关闭输出流
                                out.close();
                            }
                            if (workbook != null) {
                                //关闭工作表
                                workbook.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        //避免上层空指针错误，永远不返回null
        return mailReceiverList;
    }

    public List<Map<String, Object>> selectDivExcel(String tableName, String groupBy) {
        assertSafeSqlIdentifier(tableName, "表名");
        assertSafeSqlIdentifier(groupBy == null ? null : groupBy.replaceAll("^\\[|\\]$", ""), "分组字段");
        return mailTablesMapper.selectGroupBy(tableName.trim(), groupBy.trim());
    }

    /**
     * 校验表名/字段名，降低 ${} 动态 SQL 注入风险（仅允许字母数字下划线）
     */
    private void assertSafeSqlIdentifier(String name, String label) {
        AssertUtil.isTrue(StringUtils.isBlank(name), label + "不能为空");
        AssertUtil.isTrue(!name.matches("^[A-Za-z_][A-Za-z0-9_-]*$"), label + "不合法：" + name);
    }

    private boolean hasValidExportFiles(List<MailReceiver> exported) {
        if (exported == null || exported.isEmpty()) {
            return false;
        }
        for (MailReceiver item : exported) {
            if (item != null && StringUtils.isNotBlank(item.getPath()) && new File(item.getPath()).exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否仅为合并子报表（不应独立发信）：
     * 1) contact 配置为 NO
     * 2) 被其他未删除报表的 contact 引用（如 IHPONH.contact=IHPONL）
     */
    private boolean isMergeChildOnly(Mail mail) {
        if (mail == null || StringUtils.isBlank(mail.getTableName())) {
            return false;
        }
        if (StringUtils.isNotBlank(mail.getContact()) && "NO".equalsIgnoreCase(mail.getContact().trim())) {
            return true;
        }
        List<Mail> parents = this.list(new QueryWrapper<Mail>()
                .eq("isnull(is_del,0)", 0)
                .isNotNull("contact")
                .ne("contact", "")
                .ne("table_name", mail.getTableName()));
        if (parents == null || parents.isEmpty()) {
            return false;
        }
        String selfName = mail.getTableName().trim();
        for (Mail parent : parents) {
            if (parent == null || StringUtils.isBlank(parent.getContact())
                    || "NO".equalsIgnoreCase(parent.getContact().trim())) {
                continue;
            }
            for (String c : parent.getContact().split(";")) {
                if (selfName.equalsIgnoreCase(c == null ? "" : c.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 关联报表附件按 groupBy 值对齐：
     * - 主/关联双方都有 groupKey 时，仅合并相同分组（如 Meow 只带 Meow 附件）
     * - 任一方无 groupKey（未分组）时，全部合并（兼容旧逻辑）
     */
    private List<String> matchExtrasByGroupKey(MailReceiver mailGroup, List<MailReceiver> mergedExtras) {
        List<String> matched = new ArrayList<>();
        if (mergedExtras == null || mergedExtras.isEmpty()) {
            return matched;
        }
        String mainKey = mailGroup == null || mailGroup.getGroupKey() == null ? "" : mailGroup.getGroupKey().trim();
        for (MailReceiver extra : mergedExtras) {
            if (extra == null || StringUtils.isBlank(extra.getPath()) || !new File(extra.getPath()).exists()) {
                continue;
            }
            String extraKey = extra.getGroupKey() == null ? "" : extra.getGroupKey().trim();
            if (StringUtils.isBlank(mainKey) || StringUtils.isBlank(extraKey) || mainKey.equalsIgnoreCase(extraKey)) {
                matched.add(extra.getPath());
            }
        }
        return matched;
    }

    private String extractFileBaseName(String path) {
        if (StringUtils.isBlank(path)) {
            return "";
        }
        int lastSlash = Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/'));
        int dotIdx = path.lastIndexOf('.');
        if (lastSlash != -1 && dotIdx > lastSlash) {
            return path.substring(lastSlash + 1, dotIdx);
        }
        if (lastSlash != -1 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    /** 如需压缩且附件仍是 xlsx，则现场压成 zip */
    private String prepareAttachPath(Mail mail, String attachPath) {
        if (!Boolean.TRUE.equals(mail.getIsRar())) {
            return attachPath;
        }
        if (attachPath != null && attachPath.toLowerCase().endsWith(".zip")) {
            return attachPath;
        }
        File source = new File(attachPath);
        String zipFileName = extractFileBaseName(attachPath) + ".zip";
        File zipOutFile = new File(source.getParentFile(), zipFileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(zipOutFile)) {
            ZipUtils.toZip(attachPath, fileOutputStream, true);
            if (source.exists() && source.isFile()) {
                source.delete();
            }
            return zipOutFile.getAbsolutePath();
        } catch (Exception e) {
            systemLogService.addToLog(zipFileName, "压缩失败，源文件保留");
            throw new RuntimeException("压缩失败，源文件保留", e);
        }
    }

    private String sanitizeSheetName(String raw, int index) {
        String sheetName = StringUtils.isBlank(raw) ? ("Sheet" + (index + 1)) : raw.trim();
        sheetName = sheetName.replaceAll("[\\\\/?*\\[\\]:]", "_");
        if (sheetName.length() > 31) {
            sheetName = sheetName.substring(0, 31);
        }
        if (StringUtils.isBlank(sheetName)) {
            sheetName = "Sheet" + (index + 1);
        }
        return sheetName;
    }

    private String joinPath(String dir, String fileName) {
        if (StringUtils.isBlank(dir)) {
            return fileName;
        }
        String normalized = dir.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        if (!normalized.endsWith(File.separator)) {
            normalized = normalized + File.separator;
        }
        return normalized + fileName;
    }

    /**
     * 按逗号拆分导出列，忽略方括号 [] 内的逗号。
     */
    private List<String> splitOutColumns(String outColumn) {
        List<String> columns = new ArrayList<>();
        if (StringUtils.isBlank(outColumn)) {
            return columns;
        }
        StringBuilder current = new StringBuilder();
        int bracketDepth = 0;
        for (int i = 0; i < outColumn.length(); i++) {
            char c = outColumn.charAt(i);
            if (c == '[') {
                bracketDepth++;
                current.append(c);
            } else if (c == ']') {
                if (bracketDepth > 0) {
                    bracketDepth--;
                }
                current.append(c);
            } else if (c == ',' && bracketDepth == 0) {
                String part = current.toString().trim();
                if (StringUtils.isNotBlank(part)) {
                    columns.add(part);
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        String last = current.toString().trim();
        if (StringUtils.isNotBlank(last)) {
            columns.add(last);
        }
        return columns;
    }

    /**
     * 解析单列表达式，返回 [原字段名, Excel标题别名]。
     * 兼容：
     * 1) H1POF AS [PO Hardcopy (1=By Planner,2=By Buyer)]
     * 2) [DEBNO] [Batch No.]
     * 3) DEBNO [Batch No.]
     * 4) [DEBNO] / DEBNO（无别名）
     */
    private String[] parseOutColumnExpr(String expr) {
        if (StringUtils.isBlank(expr)) {
            return null;
        }
        expr = expr.trim();
        String realField;
        String aliasRaw;
        if (expr.toUpperCase().contains(" AS ")) {
            String[] parts = expr.split("(?i) AS ", 2);
            realField = parts[0].trim();
            aliasRaw = parts[1].trim();
        } else {
            // [Field] [Alias] 或 Field [Alias]
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("^(\\[[^\\]]+\\]|[^\\s\\[\\]]+)\\s+(\\[[^\\]]+\\])$")
                    .matcher(expr);
            if (matcher.matches()) {
                realField = matcher.group(1).trim();
                aliasRaw = matcher.group(2).trim();
            } else {
                realField = expr;
                aliasRaw = expr;
            }
        }
        String fieldKey = stripSqlBrackets(realField);
        String aliasKey = stripSqlBrackets(aliasRaw);
        if (StringUtils.isBlank(fieldKey)) {
            return null;
        }
        if (StringUtils.isBlank(aliasKey)) {
            aliasKey = fieldKey;
        }
        return new String[]{fieldKey, aliasKey};
    }

    private String stripSqlBrackets(String name) {
        if (name == null) {
            return null;
        }
        return name.trim().replaceAll("^\\[|\\]$", "").trim();
    }

    /**
     * 设置第一行标题
     */
    private void setRowTitle(List<String> titles, SXSSFSheet sheet, CellStyle cellStyle1) {
        SXSSFRow row1 = sheet.createRow(0);
        if (titles == null || titles.isEmpty()) {
            return;
        }
        for (int i = 0; i < titles.size(); i++) {
            SXSSFCell cell1 = row1.createCell(i);
            cell1.setCellValue(titles.get(i));
            cell1.setCellStyle(cellStyle1);
        }
    }

    /**
     * 设置其他行
     * @param sheet 表格
     * @param cellStyle2 格式2
     * @param titleColumns 导出字段（与结果集 Map 的 key 对应）
     * @param tableList 表值
     * @param insertRow 是否插入空行
     */
    private void setOtherRow(SXSSFSheet sheet, CellStyle cellStyle2, List<String> titleColumns, List<LinkedHashMap<String, Object>> tableList, Boolean insertRow) {
        if (titleColumns == null) {
            titleColumns = Collections.emptyList();
        }
        // 默认数据从第1行开始（标题在第0行）
        int dataStartRow = 1;
        if (insertRow) {
            SXSSFRow emptyRow = sheet.createRow(1);
            for (int j = 0; j < titleColumns.size(); j++) {
                //插入行
                SXSSFCell cells = emptyRow.createCell(j);
                cells.setCellValue("");
                cells.setCellStyle(cellStyle2);
            }
            dataStartRow = 2;
        }
        int rowIndex = dataStartRow;
        for (LinkedHashMap<String, Object> rowData : tableList) {
            // 跳过可能插入的空占位
            if (rowData == null) {continue;}
            SXSSFRow rows = sheet.createRow(rowIndex++);
            int colIndex = 0;
            for (String colName: titleColumns) {
                //其他数据
                SXSSFCell cells = rows.createCell(colIndex++);
                Object cellVal = getRowValueIgnoreCase(rowData, colName);
                // 设置单元格值（复用原有的类型判断逻辑）
                setCellValue(cells, cellVal);
                cells.setCellStyle(cellStyle2);
            }
        }
    }

    /**
     * 从数据源，从xfbps01读取表
     *
     * @param tableName 表名
     * @return 返回list map对象
     */
    public List<LinkedHashMap<String, Object>> selectTables(String tableName, @Param(Constants.WRAPPER) Wrapper<Object> queryWrapper){
        assertSafeSqlIdentifier(tableName, "表名");
        return mailTablesMapper.selectTables(tableName,queryWrapper);
    }

    public List<MailReceiver> selectGroupBy(Integer mailId){
        MailQuery mailQuery = new MailQuery();
        mailQuery.setId(mailId);
        return this.receiverToList(mailQuery);
    }

    @Override
    public List<MailReceiver> receiverList(MailQuery mailQuery) {
        //分页，两个参数，一个当前页，一个条数
//        IPage<MailReceiver> page = new Page<>(mailQuery.getPage(), mailQuery.getLimit());
        QueryWrapper<MailReceiver> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isnull(is_del,0)",0);
        if (mailQuery.getId() != null) {
            queryWrapper.eq("mail_id", mailQuery.getId());
        }
        //将查询结果返回给Page
//        page = mailReceiverMapper.selectPage(page, queryWrapper);
//        return PageResultUtil.getResult(page.getTotal(), page.getRecords());
        return mailReceiverMapper.selectList(queryWrapper);
    }

    @Override
    public List<MailReceiver> receiverToList(MailQuery mailQuery) {
        QueryWrapper<MailReceiver> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isnull(is_del,0)",0);
        if (mailQuery.getId() != null) {
            queryWrapper.eq("mail_id", mailQuery.getId());
        }
        return mailReceiverMapper.selectList(queryWrapper);
    }

    @Override
    public Mail queryMail(MailQuery mailQuery) {
        QueryWrapper<Mail> queryWrapper = new QueryWrapper<Mail>();
        if (mailQuery.getId() != null) {
            queryWrapper.eq("id", mailQuery.getId());
        }
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, transactionManager = "xfstd01Manager")   //事务控制
    public void saveReceiver(List<MailReceiver> receiverList, Mail mail) {
        AssertUtil.isTrue(receiverList.isEmpty() || receiverList.size() == 0, "请输入资料");
        for (MailReceiver mailReceiver : receiverList) {
            AssertUtil.isTrue(Boolean.TRUE.equals(mail.getIsExport()), "已设置只导出，不需要设置收件者");
            AssertUtil.isTrue(StringUtils.isBlank(mailReceiver.getReceiver()), "请设置收件者");
            AssertUtil.isTrue(!EmailUtil.checkMultiEmail(mailReceiver.getReceiver()), "收件者邮箱格式不正确");
            AssertUtil.isTrue(!EmailUtil.checkMultiEmail(mailReceiver.getCc()), "抄送者邮箱格式不正确");
            AssertUtil.isTrue(!EmailUtil.checkMultiEmail(mailReceiver.getBcc()), "秘密抄送者邮箱格式不正确");
            //如果前端没有传mailId过来就用mail的id
            mailReceiver.setMailId(NumberUtil.isEmpty(mailReceiver.getMailId()) ? mail.getId() : mailReceiver.getMailId());
            mailReceiver.setIsDel(false);
            if (NumberUtil.isEmpty(mailReceiver.getId())) {
                AssertUtil.isTrue(mailReceiverMapper.insert(mailReceiver) == 0, "收件者信息保存失败");
            } else {
                AssertUtil.isTrue(mailReceiverMapper.updateById(mailReceiver) == 0, "收件者信息保存失败");
            }
        }
    }

    @Override
    public void deleteReceiver(Integer id) {
        AssertUtil.isTrue(id == null || id == 0, "请正确选择数据");
        MailReceiver mailReceiver = new MailReceiver();
        mailReceiver.setId(id);
        mailReceiver.setIsDel(true);
        Mail mail = this.baseMapper.selectById(mailReceiverMapper.selectById(id).getMailId());
        systemLogService.addToLog(mail, "收件人"+ mail.getTableName()+"信息删除成功");
        AssertUtil.isTrue(mailReceiverMapper.updateById(mailReceiver) == 0, "收件人信息更新失败");
    }

    private Object getRowValueIgnoreCase(Map<String, Object> row, String fieldName) {
        if (row == null || StringUtils.isBlank(fieldName)) {
            return null;
        }
        if (row.containsKey(fieldName)) {
            return row.get(fieldName);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(fieldName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 抽取的公共方法：根据对象类型设置单元格值
     */
    private void setCellValue(SXSSFCell cell, Object cellVal) {
        if (cellVal instanceof BigDecimal) {
            BigDecimal num = (BigDecimal) cellVal;
            cell.setCellValue(NumberUtil.stripTrailingZeros(num).doubleValue());
        } else if (cellVal instanceof Timestamp) {
            cell.setCellValue(cellVal.toString());
        } else if (cellVal == null) {
            cell.setCellValue("");
        } else if (cellVal instanceof Date) {
            cell.setCellValue((Date) cellVal);
        } else if (cellVal instanceof Integer) {
            cell.setCellValue((Integer) cellVal);
        } else if (cellVal instanceof Long) {
            cell.setCellValue((Long) cellVal);
        } else if (cellVal instanceof Double) {
            cell.setCellValue((Double) cellVal);
        } else if (cellVal instanceof Boolean) {
            cell.setCellValue((Boolean) cellVal);
        } else {
            cell.setCellValue(String.valueOf(cellVal));
        }
    }

}
