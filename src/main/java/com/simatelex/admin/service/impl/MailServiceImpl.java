package com.simatelex.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.ChannelSftp;
import com.simatelex.admin.config.datasource.DataSourceKey;
import com.simatelex.admin.config.datasource.TargetDataSource;
import com.simatelex.admin.config.mail.SetCron;
import com.simatelex.admin.mappers.xfbps01.MailTablesMapper;
import com.simatelex.admin.mappers.xfstd01.MailMapper;
import com.simatelex.admin.pojo.Mail;
import com.simatelex.admin.pojo.SFTPChannel;
import com.simatelex.admin.pojo.SFTPConstants;
import com.simatelex.admin.query.MailQuery;
import com.simatelex.admin.service.IMailService;
import com.simatelex.admin.service.ISystemLogService;
import com.simatelex.admin.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.xssf.usermodel.*;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        AssertUtil.isTrue(mail == null, "请输入数据");
        AssertUtil.isTrue(mail.getTableName().split("-").length > 2, "符号-不能超过1个");
//        AssertUtil.isTrue((!mail.getIsExport() || !mail.getIsFtpUpload()) && StringUtils.isBlank(mail.getReceiver()) && !mail.getIsFtpUpload(), "收件人不能为空");
        AssertUtil.isTrue(mail.getDivExcel() && StringUtils.isBlank(mail.getGroupBy()), "分组数据不正确");
        AssertUtil.isTrue(mail.getSendTarget().equals("每周") && mail.getSendTime().indexOf("-") == mail.getSendTime().lastIndexOf("-"), "格式错误");
        mail.setIsDel(false);
        AssertUtil.isTrue(!this.save(mail), "添加失败！");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, transactionManager = "xfstd01Manager")   //事务控制
    public void updateMail(Mail mail, boolean isRun) {
        AssertUtil.isTrue(mail == null, "请输入数据");
        AssertUtil.isTrue(mail.getTableName().split("-").length > 2, "符号-不能超过1个");
//        AssertUtil.isTrue((!mail.getIsExport() || !mail.getIsFtpUpload()) && StringUtils.isBlank(mail.getReceiver()) && !mail.getIsFtpUpload(), "收件人不能为空");
        AssertUtil.isTrue(mail.getDivExcel() && StringUtils.isBlank(mail.getGroupBy()), "分组数据不正确");
        AssertUtil.isTrue(mail.getSendTarget().equals("每周") && mail.getSendTime().indexOf("-") == mail.getSendTime().lastIndexOf("-"), "格式错误");
//        Mail oldMail = this.getById(mail.getId());
        AssertUtil.isTrue(!this.updateById(mail), "更新失败");
        if (isRun) {
            emailScheduleService.modifyJob(mail.getTableName(), "group_" + mail.getTableName(), SetCron.getCron(mail));
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, transactionManager = "xfstd01Manager")   //事务控制
    public void deleteMail(Integer[] ids) {
        AssertUtil.isTrue(ids == null || ids.length == 0, "请正确选择数据");
        List<Mail> mailList = new ArrayList<>();
        for (Integer id : ids) {
            Mail mail = this.getById(id);
            mail.setIsDel(true);
            mailList.add(mail);
            systemLogService.addToLog(mail, "邮箱信息删除成功");
        }
        AssertUtil.isTrue(!this.updateBatchById(mailList), "数据更新失败");
    }

    @Override
    public void sendMail(Mail mail) {
        try {
            //如果是ftp上传
            if (mail.getIsFtpUpload()){
                //拆分YYYYMMDD;-1格式
                String[] tableStyle = mail.getTableStyle().split(";");
                //根据需求设置后缀的日期格式
                String customDate = DateUtil.formatCustomDate(LocalDateTime.now().plusDays(Long.parseLong(tableStyle[1])), tableStyle[0]);
                String localPath = mail.getFtpLocalPath() + (StringUtils.isNotBlank(mail.getTablePrefix()) ? mail.getTablePrefix() : mail.getTableName()) + customDate + "."+ mail.getTableSuffix();
                if (StringUtils.isNotBlank(mail.getSubject())) {
                    //根据字段查需要检测的报表
                    Mail checkMail = this.baseMapper.selectOne(new QueryWrapper<Mail>().eq("table_name", mail.getSubject()));
                    if (checkMail != null) {
                        String[] checkStyle = checkMail.getTableStyle().split(";");
                        String checkDate = DateUtil.formatCustomDate(LocalDateTime.now().plusDays(Long.parseLong(checkStyle[1])), checkStyle[0]);
                        String checkPath = checkMail.getFtpLocalPath() + (StringUtils.isNotBlank(checkMail.getTablePrefix()) ? checkMail.getTablePrefix() : checkMail.getTableName()) + checkDate + "." + checkMail.getTableSuffix();
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
                //如果只导出
                if (mail.getIsExport()) {
                    exportExcelPath(mail);
                    this.truncateTable(mail);
//                    log.info(mailTablesMapper.truncateTable(mail.getTableName()) ? "清空表" + mail.getTableName() + "成功！" : "清空表" + mail.getTableName() + "失败！");
                } else {
                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                    //加上true代表此封邮件是多资源的，多附件的
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    List<String> attachPathList = new ArrayList<>();
                    //得到表名，为了清理数据使用
                    List<String> tableNameList = new ArrayList<>();
                    if (exportExcelPath(mail) != null) {
                        //得到导出的数据路径,为了防止第一个导出路径为空
                        attachPathList.addAll(exportExcelPath(mail));
                        //先将本来的表插入
                        tableNameList.add(mail.getTableName());
                    }
                    //得到导出文件的文件名
                    List<String> tableList = new ArrayList<>();
                    if (StringUtils.isNotBlank(mail.getContact())) {    //如果需要报表合并
                        for (String contact : mail.getContact().split(";")) {
                            Mail mailContactList = this.baseMapper.selectOne(new QueryWrapper<Mail>().eq("table_name", contact));
                            List<String> contactList = new ArrayList<>();
                            if (exportExcelPath(mailContactList) != null) {
                                contactList.addAll(exportExcelPath(mailContactList));
                                tableNameList.add(contact);
                            }
                            attachPathList.addAll(contactList);
                        }
                    }
                    //主旨
                    /*helper.setSubject(mail.getSubject());
                    //设置收件者
//                    helper.setTo(mail.getReceiver().split(";"));
                    //设置发送者
                    helper.setFrom(mail.getSender());
                    if (StringUtils.isNotBlank(mail.getCc())) {
                        //设置抄送者
                        helper.setCc(mail.getCc());
                    }
                    if (StringUtils.isNotBlank(mail.getBcc())) {
                        //设置秘密抄送
                        helper.setBcc(mail.getBcc());
                    }*/
//        helper.setText("<h1>图片：</h1><img src='cid:"+ id +"'/>",true); //发送html的正文
                    //主体内容
                    helper.setText(mail.getBody());
            /*FileSystemResource resource = new FileSystemResource(new File(attachPath));//转换成资源路径
            helper.addInline("test", resource);//添加行*/
                    //如果不需要发送附件的，就直接发送出去，不需要再添加附件
                    if (mail.getEmptyAttach()) {
                        //发送邮件
                        mailSender.send(mimeMessage);
                    } else {
                        //添加附件，可以设置多个附件
                        try {
                            String zipPath = null;
                            //如果有附件的话
                            if (attachPathList != null && attachPathList.size() > 0) {
                                for (String attachPath : attachPathList) {
//                helper.addAttachment(MimeUtility.encodeWord(mail.getTableName()+ DateUtil.formatCustomDate(LocalDateTime.now().plusDays(Long.parseLong(mail.getTableStyle().split(";")[1])), mail.getTableStyle().split(";")[0]) +".xlsx","UTF-8","B"), resource); //此方法不可行
                    /*MimeBodyPart bodyPart = new MimeBodyPart();//设置消息正文
                    bodyPart.setText(mail.getBody(), "UTF-8"); //设置正文编码为UTF-8
                    //单个文件上传
                    Multipart multipart = new MimeMultipart();
                    //设置相关文件
                    MimeBodyPart filePart = new MimeBodyPart();*/
                                    String datePrefix = DateUtil.formatCustomDate(LocalDateTime.now().plusDays(Long.parseLong(mail.getTableStyle().split(";")[1])), mail.getTableStyle().split(";")[0]);
                                    String fileFormat = ".xlsx";
                                    //如果需要压缩
                                    if (mail.getIsRar()) {
                                        fileFormat = ".zip";
                                        FileOutputStream fileOutputStream = new FileOutputStream(new File(attachPath.substring(0, attachPath.lastIndexOf("\\")) + "\\" + mail.getTableName() + datePrefix + fileFormat));
                                        zipPath = ZipUtils.toZip(attachPath, fileOutputStream, true) + "\\" + mail.getTableName() + datePrefix + fileFormat;
                                    }
                                    FileDataSource dataSource = new FileDataSource(mail.getIsRar() ? zipPath : attachPath);
//                    System.err.println(dataSource.getName());
                                    //如果有别名就=文件名+tableStyle+文件后缀名,否则就是表名+tableStyle+文件后缀名
                                    String tableName = dataSource.getName().substring(0, dataSource.getName().lastIndexOf(".")) + fileFormat;
                                    if (StringUtils.isNotBlank(mail.getTablePrefix())) {
                                        tableName = mail.getTablePrefix() + datePrefix + fileFormat;
                                    }
                    /*filePart.setFileName(MimeUtility.encodeText(MimeUtility.encodeText(tableName)));
                    //文件处理
                    filePart.setDataHandler(new DataHandler(dataSource));
                    //放入正文(有先后顺序，所以在正文后面)
                    multipart.addBodyPart(filePart);
                    multipart.addBodyPart(bodyPart);

                    //设置邮件内容，混合模式
                    mimeMessage.setContent(multipart);*/
                                    helper.addAttachment(tableName, dataSource);
                                    tableList.add(tableName);
                                    systemLogService.addToLog(mail, tableName + "发送成功！");
                                }
                                //发送邮件
                                mailSender.send(mimeMessage);
                                truncateTable(mail, tableNameList);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            systemLogService.addToLog(mail,"邮件发送异常！");
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
            mailTablesMapper.truncateTable(truncateTable);
            systemLogService.addToLog(mail, truncateTable + "清理成功！");
        }
    }

    /**
     * 第二种方式：单独清理
     * @param mail
     */
    private void truncateTable(Mail mail) {
            mailTablesMapper.truncateTable(mail.getTableName());
            systemLogService.addToLog(mail, mail.getTableName() + "清理成功！");
    }

    @Override
    public void runEmail(boolean run){
        if (run){
            List<Mail> mailList = this.baseMapper.selectList(new QueryWrapper<Mail>().eq("is_del", 0));
            for (int i = 0; i < mailList.size(); i++) {
                Mail mail = mailList.get(i);
                String cron = SetCron.getCron(mail);
                try {
                    emailScheduleService.scheduleEmailJob(mail.getTableName(), "group_"+mail.getTableName(), mail,cron);
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            }
        } else {
            emailScheduleService.standby();
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
     * @param mail
     * @return
     */
    public List<String> exportExcelPath(Mail mail){
        //分组数据
        List<Map<String, Object>> groupBy = new ArrayList<>();
        //查询条件
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        //返回的路径
        List<String> pathList = new ArrayList<>();
        //sheet个数
        int sheetCount = 1;
        //工作表个数
        int workbookCount = 1;
        //  源路径
        String sourcePath = null;
        //表名
        /*String tableName = null;  //暂时先不用这个逻辑
        if (mail.getTableName().contains("-")) {
            tableName = mail.getTableName().split("-")[0];
        } else {
            tableName =  mail.getTableName();
        }*/
        String tableName = mail.getTableName();
        /*if (StringUtils.isNotBlank(mail.getGroupBy())) { //待处理   //分table
            groupBy = this.selectGroupBy(mail.getTableName(), mail.getGroupBy());//select distinct ${groupBy} from ${tableName}
            workbookCount = groupBy.size(); //设置工作表个数
        }*/
        //如果根据group by分sheet
        if (mail.getDivExcel()){
            //如果要分sheet，就只能是一个工作表
            workbookCount = 1;
            //分组之后的sheet个数
            sheetCount = groupBy.size();
        }
        List<Map<String, Object>> tableList = new ArrayList<>();
        //拆分YYYYMMDD;-1格式
        String[] tableStyle = mail.getTableStyle().split(";");
        //根据需求设置后缀的日期格式
        String customDate = DateUtil.formatCustomDate(LocalDateTime.now().plusDays(Long.parseLong(tableStyle[1])), tableStyle[0]);
        //待处理  //查询xfbps01中的表数据   select * from ${tableName} where ${groupBy}=值
        tableList = this.selectTables(mail.getTableName(), queryWrapper);
        //要有数据才
        if (tableList.size() > 0) {
            //循环工作表
            for (int i = 0; i < workbookCount; i++) {
                //如果工作表是多个
                if (workbookCount > 1) {
                    //为了保证第二次循环不重复查询第一次的数据
                    queryWrapper.clear();
                    //添加查询条件
                    queryWrapper.eq(mail.getGroupBy(), groupBy.get(i).get(mail.getGroupBy()));
                    //工作表名=报表名_分组值
                    tableName = mail.getTableName() + "_" + groupBy.get(i).get(mail.getGroupBy());
                }
                //创建工作簿
                XSSFWorkbook workbook = new XSSFWorkbook();
                //循环sheet表
                for (int j = 0; j < sheetCount; j++) {
                    //如果sheet表是多个
                    if (sheetCount > 1) {
                        //为了保证第二次循环不重复查询第一次的数据
                        queryWrapper.clear();
                        //添加查询条件
                        queryWrapper.eq(mail.getGroupBy(), groupBy.get(i).get(mail.getGroupBy()));
                        //sheet表名=分组值
                        tableName = (String) groupBy.get(i).get(mail.getGroupBy());
                    }
                    //工作表
                    XSSFSheet sheet = workbook.createSheet();
                    //设置sheet表名，如果只有一个sheet就是报表名，否则就是分组值
                    workbook.setSheetName(j, sheetCount == 1 ? tableName : (String) groupBy.get(j).get(mail.getGroupBy()));
                    //设置首行加粗字体
                    XSSFCellStyle cellStyle1 = ExcelStyleUtil.getCellStyle2(workbook, ExcelStyleUtil.getXssfFont15(workbook, StringUtils.isNotBlank(mail.getFonts()) ? mail.getFonts() : "Times New Roman", mail.getFontSize() != null ? mail.getFontSize() : 10));
                    //设置其他行不加粗
                    XSSFCellStyle cellStyle2 = ExcelStyleUtil.getCellStyle2(workbook, ExcelStyleUtil.getXssfFont16(workbook, StringUtils.isNotBlank(mail.getFonts()) ? mail.getFonts() : "Times New Roman", mail.getFontSize() != null ? mail.getFontSize() : 9));
                    //设置标题行
                    setRow1(mail, tableList, sheet, cellStyle1);
                    //设置数据行
                    setOtherRow(sheet, cellStyle2, tableList, mail.getInsertRow());
                }
                //输出到模板中
                BufferedOutputStream out = null;
                try {
                    //格式：G:\Program\doc\test\test.xlsx
                    sourcePath = new File(System.getProperty("user.dir")).getParent() + "\\doc\\" + mail.getTableName() + "\\" + tableName + customDate + ".xlsx";
                    File sourceFile = new File(sourcePath);
                    //获取上级目录
                    boolean dr = sourceFile.getParentFile().mkdirs();
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
                    //如果只需要导出，不需要发送邮件
                    if (StringUtils.isNotBlank(mail.getPath())) {
                        String targetPath = mail.getPath() + "\\" + mail.getTableName() + "\\" + tableName + customDate + ".xlsx";
                        //新建file到目标路径
                        File targetFile = new File(targetPath);
                        //如果目标路径的文件夹不存在则创建文件夹
                        if (!new File(mail.getPath()).exists()) {
                            targetFile.mkdirs();
                        }
                        //将源文件复制到目标路径文件中
                        Files.copy(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
                    }
                    //添加路径到返回路径中
                    pathList.add(sourcePath);
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
            log.info(mail.getTableName() + "导出成功！");
            return pathList;
        } else {
            return null;
        }
    }

    /**
     * 从数据源，从xfbps01读取表
     * @param tableName 表名
     * @return  返回list map对象
     */
    /*@TargetDataSource(dataSourceKey = DataSourceKey.DB_XFBPS01)
    public List<Map<String, Object>> selectTables(String tableName,@Param(Constants.WRAPPER) Wrapper<Object> queryWrapper){
        return mailTablesMapper.selectTables(tableName,queryWrapper);
    }

    @TargetDataSource(dataSourceKey = DataSourceKey.DB_XFBPS01)
    public List<Map<String, Object>> selectGroupBy(String tableName, String groupBy){
        return mailTablesMapper.selectGroupBy(tableName, groupBy);
    }*/
    /**
     * 设置第一行标题
     * @param mail  邮箱
     * @param tableList
     * @param sheet 表格
     * @param cellStyle1 格式1
     */
    private void setRow1(Mail mail, List<Map<String, Object>> tableList, XSSFSheet sheet, XSSFCellStyle cellStyle1) {
        XSSFRow row1 = sheet.createRow(0);
        String[] split;
        if (StringUtils.isNotBlank(mail.getOutColumn())) {  //如果导出的字段不为空，就代表要按照自己自定义的字段名查询
            String outColumn = mail.getOutColumn();
            split = outColumn.split(",");
        } else {
            split = tableList.get(0).keySet().size() > 0 ? tableList.get(0).keySet().toArray(new String[tableList.get(0).size()]) : null;
        }
        if (split.length > 0) {
            for (int i = 0; i < split.length; i++) {
                XSSFCell cell1 = row1.createCell(i); //第1行标题
                String column = split[i].substring(split[i].indexOf("AS") + 1);
                cell1.setCellValue(column);
                cell1.setCellStyle(cellStyle1); //将上面设置的样式放进第一行第一列中
            }
        }
    }

    /**
     * 设置其他行
     * @param sheet 表格
     * @param cellStyle2 格式2
     * @param tableList 表值
     * @param insertRow
     */
    private void setOtherRow(XSSFSheet sheet, XSSFCellStyle cellStyle2, List<Map<String, Object>> tableList, Boolean insertRow) {
        if (insertRow) {
            XSSFRow row1 = sheet.createRow(1);
            Object[] columns = tableList.get(0).values().toArray();
            for (int j = 0; j < columns.length; j++) {
                XSSFCell cells = row1.createCell(j); //插入行
                cells.setCellValue("");
                cells.setCellStyle(cellStyle2);
            }
            tableList.add(0,null);
        }
        for (int i = 0; i < tableList.size(); i++) {
            Map<String, Object> tableDataMap = tableList.get(i);
            XSSFRow rows = sheet.createRow(i+1);
            if (tableDataMap != null) {
                Object[] columns = tableDataMap.values().toArray();
                for (int j = 0; j < columns.length; j++) {
                    XSSFCell cells = rows.createCell(j); //其他数据
                    cells.setCellValue(String.valueOf(columns[j]));
                    cells.setCellStyle(cellStyle2);
                }
            }
        }
    }

    /**
     * 从数据源，从xfbps01读取表
     * @param tableName 表名
     * @return  返回list map对象
     */
    @TargetDataSource(dataSourceKey = DataSourceKey.DB_XFBPS01)
    public List<Map<String, Object>> selectTables(String tableName,@Param(Constants.WRAPPER) Wrapper<Object> queryWrapper){
        return mailTablesMapper.selectTables(tableName,queryWrapper);
    }

    @TargetDataSource(dataSourceKey = DataSourceKey.DB_XFBPS01)
    public List<Map<String, Object>> selectGroupBy(String tableName, String groupBy){
        return mailTablesMapper.selectGroupBy(tableName, groupBy);
    }
}
