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
        queryWrapper.eq("isnull(is_del,0)", 0);//判断是否已经删除
        //将查询结果返回给Page
        page = this.baseMapper.selectPage(page, queryWrapper);
        return PageResultUtil.getResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void saveMail(Mail mail) {
        AssertUtil.isTrue(mail == null, "请输入数据");
        AssertUtil.isTrue((!mail.getIsExport() || !mail.getIsFtpUpload()) && StringUtils.isBlank(mail.getReceiver()) && !mail.getIsFtpUpload(), "收件人不能为空");
        AssertUtil.isTrue(mail.getDivExcel() && StringUtils.isBlank(mail.getGroupBy()), "分组数据不正确");
        AssertUtil.isTrue(mail.getSendTarget().equals("每周") && mail.getSendTime().indexOf("-") == mail.getSendTime().lastIndexOf("-"), "格式错误");
        mail.setIsDel(false);
        AssertUtil.isTrue(!this.save(mail), "添加失败！");
    }

    @Override
    public void updateMail(Mail mail, boolean isRun) {
        AssertUtil.isTrue(mail == null, "请输入数据");
        AssertUtil.isTrue((!mail.getIsExport() || !mail.getIsFtpUpload()) && StringUtils.isBlank(mail.getReceiver()) && !mail.getIsFtpUpload(), "收件人不能为空");
        AssertUtil.isTrue(mail.getDivExcel() && StringUtils.isBlank(mail.getGroupBy()), "分组数据不正确");
        AssertUtil.isTrue(mail.getSendTarget().equals("每周") && mail.getSendTime().indexOf("-") == mail.getSendTime().lastIndexOf("-"), "格式错误");
//        Mail oldMail = this.getById(mail.getId());
        AssertUtil.isTrue(!this.updateById(mail), "更新失败");
        if (isRun) {
            emailScheduleService.modifyJob(mail.getTableName(), "group_" + mail.getTableName(), SetCron.getCron(mail));
        }
    }

    @Override
    public void deleteMail(Integer[] ids) {
        AssertUtil.isTrue(ids == null || ids.length == 0, "请正确选择数据");
        List<Mail> mailList = new ArrayList<>();
        for (Integer id : ids) {
            Mail mail = this.getById(id);
            mail.setIsDel(true);
            mailList.add(mail);
        }
        AssertUtil.isTrue(!this.updateBatchById(mailList), "数据更新失败");
    }

    @Override
    public void sendMail(Mail mail) {
        try {
            if (mail.getIsFtpUpload()){     //如果是ftp上传
                String[] tableStyle = mail.getTableStyle().split(";");//拆分YYYYMMDD;-1格式
                String customDate = DateUtil.formatCustomDate(LocalDateTime.now().plusDays(Long.parseLong(tableStyle[1])), tableStyle[0]);    //根据需求设置后缀的日期格式
                String localPath = mail.getFtpLocalPath() + (StringUtils.isNotBlank(mail.getTablePrefix()) ? mail.getTablePrefix() : mail.getTableName()) + customDate + "."+ mail.getTableSuffix();
                if (StringUtils.isNotBlank(mail.getSubject())) {
                    Mail checkMail = this.baseMapper.selectOne(new QueryWrapper<Mail>().eq("table_name", mail.getSubject()));//根据字段查需要检测的报表
                    if (checkMail != null) {
                        String[] checkStyle = checkMail.getTableStyle().split(";");
                        String checkDate = DateUtil.formatCustomDate(LocalDateTime.now().plusDays(Long.parseLong(checkStyle[1])), checkStyle[0]);
                        String checkPath = checkMail.getFtpLocalPath() + (StringUtils.isNotBlank(checkMail.getTablePrefix()) ? checkMail.getTablePrefix() : checkMail.getTableName()) + checkDate + "." + checkMail.getTableSuffix();
                        if (StringUtils.isNotBlank(checkPath)) {
                            if (!isFileExist(checkPath)) {
                                systemLogService.addToLog("upload_ftp",mail.getTableName()+"上传失败！原因："+mail.getSubject()+ "不存在！",mail.getTableName(),"admin");
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
                        chSftp.put(localPath, mail.getPath(), ChannelSftp.OVERWRITE);   //put第一个参数为文件完整路径，包括文件全称
                        systemLogService.addToLog("upload_ftp",mail.getTableName()+"上传成功！",mail.getTableName(),"admin");
                    }
                    chSftp.quit();
                    channel.closeChannel();
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                    System.out.println(e);
                    log.error(String.valueOf(e));
                    systemLogService.addToLog("upload_ftp",mail.getTableName()+"上传失败！原因：本地路径不存在",mail.getTableName());
                } catch (Exception e) {
                    systemLogService.addToLog("upload_ftp",mail.getTableName()+"上传失败！原因："+ (e.toString().length()>=8000 ? e.toString().substring(0,7550) : e.toString()),mail.getTableName(),"admin");
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
                if (mail.getIsExport()) {   //如果只导出
                    exportExcelPath(mail);
//                    log.info(mailTablesMapper.truncateTable(mail.getTableName()) ? "清空表" + mail.getTableName() + "成功！" : "清空表" + mail.getTableName() + "失败！");
                } else {
                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");//加上true代表此封邮件是多资源的，多附件的
                    List<String> attachPathList = new ArrayList<>();
                    List<String> tableNameList = new ArrayList<>(); //得到表名，为了清理数据使用
                    if (exportExcelPath(mail) != null) {
                        attachPathList.addAll(exportExcelPath(mail)); //得到导出的数据路径,为了防止第一个导出路径为空
                        tableNameList.add(mail.getTableName()); //先将本来的表插入
                    }
                    List<String> tableList = new ArrayList<>(); //得到导出文件的文件名
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
                    helper.setSubject(mail.getSubject());   //主旨
                    helper.setTo(mail.getReceiver().split(";"));   //设置收件者
                    helper.setFrom(mail.getSender());   //设置发送者
                    if (StringUtils.isNotBlank(mail.getCc())) {
                        helper.setCc(mail.getCc()); //设置抄送者
                    }
                    if (StringUtils.isNotBlank(mail.getBcc())) {
                        helper.setBcc(mail.getBcc());   //设置秘密抄送
                    }
//        helper.setText("<h1>图片：</h1><img src='cid:"+ id +"'/>",true); //发送html的正文
                    helper.setText(mail.getBody()); //主体内容
            /*FileSystemResource resource = new FileSystemResource(new File(attachPath));//转换成资源路径
            helper.addInline("test", resource);//添加行*/
                    //添加附件
                    // 可以设置多个附件
                    try {
                        String zipPath = null;
                        if (attachPathList != null && attachPathList.size() > 0) {  //如果有附件的话
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
                                systemLogService.addToLog("email_send",tableName+"发送成功！",mail.getTableName(),"admin");
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    mailSender.send(mimeMessage);   //发送邮件
                    for (String truncateTable : tableNameList) {
                        mailTablesMapper.truncateTable(truncateTable);
                        systemLogService.addToLog("email_truncate",truncateTable+"清理成功！",mail.getTableName(),"admin");
                    }
                }
            }
        } catch (Exception e) {
            systemLogService.addToLog("email_send","邮件发送异常！",mail.getTableName(),"admin");
            e.printStackTrace();
        }
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
     * 1.判断是不是分table，groupBy字段不为空
     * 2.判断是不是分sheet，divExcel为true
     * @param mail
     * @return
     */
    public List<String> exportExcelPath(Mail mail){
        List<Map<String, Object>> groupBy = new ArrayList<>();  //分组数据
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();   //查询条件
        List<String> pathList = new ArrayList<>();  //返回的路径
        int sheetCount = 1; //sheet个数
        int workbookCount = 1;  //工作表个数
        String sourcePath;  //  源路径
        String tableName = mail.getTableName(); //表名
        /*if (StringUtils.isNotBlank(mail.getGroupBy())) { //待处理   //分table
            groupBy = this.selectGroupBy(mail.getTableName(), mail.getGroupBy());//select distinct ${groupBy} from ${tableName}
            workbookCount = groupBy.size(); //设置工作表个数
        }*/
        if (mail.getDivExcel()){    //如果根据group by分sheet
            workbookCount = 1;  //如果要分sheet，就只能是一个工作表
            sheetCount = groupBy.size();//分组之后的sheet个数
        }
        List<Map<String, Object>> tableList = new ArrayList<>();
        String[] tableStyle = mail.getTableStyle().split(";");//拆分YYYYMMDD;-1格式
        String customDate = DateUtil.formatCustomDate(LocalDateTime.now().plusDays(Long.parseLong(tableStyle[1])), tableStyle[0]);    //根据需求设置后缀的日期格式
        tableList = this.selectTables(mail.getTableName(), queryWrapper); //待处理  //查询xfbps01中的表数据   select * from ${tableName} where ${groupBy}=值
        if (tableList.size() > 0) {
            for (int i = 0; i < workbookCount; i++) {   //循环工作表
                if (workbookCount > 1) {    //如果工作表是多个
                    queryWrapper.clear();   //为了保证第二次循环不重复查询第一次的数据
                    queryWrapper.eq(mail.getGroupBy(), groupBy.get(i).get(mail.getGroupBy()));  //添加查询条件
                    tableName = mail.getTableName() + "_" + groupBy.get(i).get(mail.getGroupBy());   //工作表名=报表名_分组值
                }
                XSSFWorkbook workbook = new XSSFWorkbook(); //创建工作簿
                for (int j = 0; j < sheetCount; j++) { //循环sheet表
                    if (sheetCount > 1) {   //如果sheet表是多个
                        queryWrapper.clear();   //为了保证第二次循环不重复查询第一次的数据
                        queryWrapper.eq(mail.getGroupBy(), groupBy.get(i).get(mail.getGroupBy())); //添加查询条件
                        tableName = (String) groupBy.get(i).get(mail.getGroupBy()); //sheet表名=分组值
                    }
                    XSSFSheet sheet = workbook.createSheet(); //工作表
                    workbook.setSheetName(j, sheetCount == 1 ? tableName : (String) groupBy.get(j).get(mail.getGroupBy())); //设置sheet表名，如果只有一个sheet就是报表名，否则就是分组值
                    //设置首行加粗字体
                    XSSFCellStyle cellStyle1 = ExcelStyleUtil.getCellStyle2(workbook, ExcelStyleUtil.getXssfFont15(workbook, StringUtils.isNotBlank(mail.getFonts()) ? mail.getFonts() : "Times New Roman", mail.getFontSize() != null ? mail.getFontSize() : 10));
                    //设置其他行不加粗
                    XSSFCellStyle cellStyle2 = ExcelStyleUtil.getCellStyle2(workbook, ExcelStyleUtil.getXssfFont16(workbook, StringUtils.isNotBlank(mail.getFonts()) ? mail.getFonts() : "Times New Roman", mail.getFontSize() != null ? mail.getFontSize() : 9));
                    setRow1(mail, tableList, sheet, cellStyle1);    //设置标题行
                    setOtherRow(sheet, cellStyle2, tableList, mail.getInsertRow()); //设置数据行
                }
                BufferedOutputStream out = null;    //输出到模板中
                try {
                    //格式：G:\Program\doc\test\test.xlsx
                    sourcePath = new File(System.getProperty("user.dir")).getParent() + "\\doc\\" + mail.getTableName() + "\\" + tableName + customDate + ".xlsx";
                    File sourceFile = new File(sourcePath);
                    boolean dr = sourceFile.getParentFile().mkdirs();   //获取上级目录
                    if (sourceFile.exists()) {    //如果此文件存在则先删除
                        sourceFile.delete();
                    }
                    out = new BufferedOutputStream(new FileOutputStream(sourcePath));   //以以上路径新建输出流
                    workbook.write(out);    //将内容写到输出流中
                    out.flush();    //flush是把流里的缓冲数据输出，flush以后还能继续使用这个OutputStream
                    if (StringUtils.isNotBlank(mail.getPath())) {   //如果只需要导出，不需要发送邮件
                        String targetPath = mail.getPath() + "\\" + mail.getTableName() + "\\" + tableName + customDate + ".xlsx";
                        File targetFile = new File(targetPath);    //新建file到目标路径
                        if (!new File(mail.getPath()).exists()) {   //如果目标路径的文件夹不存在则创建文件夹
                            targetFile.mkdirs();
                        }
                        Files.copy(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);    //将源文件复制到目标路径文件中
                    }
                    pathList.add(sourcePath);   //添加路径到返回路径中
                } catch (Exception e) {
                    e.printStackTrace();
                } finally { //释放资源
                    try {
                        if (out != null) {
                            out.close();    //关闭输出流
                        }
                        if (workbook != null) {
                            workbook.close();   //关闭工作表
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
