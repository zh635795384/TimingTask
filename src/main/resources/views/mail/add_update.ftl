<!DOCTYPE html>
<html>
<#include "../common.ftl">
<head>
    <style>
        .senderLine {
            width: 352px;
        }
        .contactLine {
            width: 353px;
        }
        .layui-textarea{
            min-height: 50px;
        }
        .layui-form-label {
            padding: 9px 9px;
        }
        .layui-form-item .layui-inline {
            margin-bottom: 0px;
            margin-right: 0px;
        }
    </style>
</head>
<body class="childrenBody">
<form class="layui-form" style="width:100%;">
    <input name="id" type="hidden" value="${(mail.id)!}"/>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline">
            <label class="layui-form-label">表名*</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input tableName" lay-verify="required" name="tableName" id="tableName" value="${(mail.tableName)!}" placeholder="请输入表名">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">别名</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input tablePrefix" name="tablePrefix" id="tablePrefix" value="${(mail.tablePrefix)!}" placeholder="请输入别名">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">请求前缀</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input postPrefix" name="postPrefix" id="postPrefix" value="${(mail.postPrefix)!}" placeholder="请输入请求前缀">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">字体</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input fonts" name="fonts" id="fonts" value="${(mail.fonts)!}" placeholder="请输入字体">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">字体大小</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input fontSize" name="fontSize" id="fontSize" value="${(mail.fontSize)!}" placeholder="请输入字体大小">
            </div>
        </div>
    </div>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline">
            <label class="layui-form-label">附件</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input attach" name="attach" id="attach" value="${(mail.attach)!}" placeholder="请输入附件">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">分组名称</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input groupBy" name="groupBy" id="groupBy" value="${(mail.groupBy)!}" placeholder="请输入分组名称">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">发件者*</label>
            <div class="layui-input-inline senderLine">
                <input type="text" class="layui-input sender" lay-verify="required" name="sender" id="sender" value="<#if '${(mail.sender)!}'=''>Noreply@simatelex.com.hk.dl<#else>${(mail.sender)!}</#if>" placeholder="请输入发件者">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">是否压缩*</label>
            <div class="layui-input-inline" style="width: 70px;">
                <input type="checkbox" id="isRar" lay-skin="switch" value="${(mail.isRar?string("true","false"))!}" lay-filter="isRar" lay-text="是|否" <#if '${(mail.isRar?string("true","false"))!}'='true'>checked</#if>>
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">是否插入行*</label>
            <div class="layui-input-inline" style="width: 70px;">
                <input type="checkbox" id="insertRow" lay-skin="switch" value="${(mail.insertRow?string("true","false"))!}" lay-filter="insertRow" lay-text="是|否" <#if '${(mail.insertRow?string("true","false"))!}'='true'>checked</#if>>
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">分sheet*</label>
            <div class="layui-input-inline" style="width: 70px;">
                <input type="checkbox" id="divExcel" lay-skin="switch" value="${(mail.divExcel?string("true","false"))!}" lay-filter="divExcel" lay-text="是|否" <#if '${(mail.divExcel?string("true","false"))!}'='true'>checked</#if>>
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">是否只导出*</label>
            <div class="layui-input-inline" style="width: 70px;">
                <input type="checkbox" id="isExport" lay-skin="switch" value="${(mail.isExport?string("true","false"))!}" lay-filter="isExport" lay-text="是|否" <#if '${(mail.isExport?string("true","false"))!}'='true'>checked</#if>>
            </div>
        </div>
    </div>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline">
            <label class="layui-form-label">连接报表</label>
            <div class="layui-input-inline contactLine">
                <input type="text" class="layui-input contact" name="contact" id="contact" value="${(mail.contact)!}" placeholder="请输入连接报表">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">导出路径</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input path" name="path" id="path" value="${(mail.path)!}" placeholder="请输入导出路径">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">发送周期*</label>
            <div class="layui-input-inline" style="width: 140px;">
                <select id="sendTarget" name="sendTarget" lay-verify="required" lay-event="sendTargetSelectId">
                    <option id="sendTarget1" class="sendTarget1" name="sendTarget" value="">请选择发送周期</option>
                    <option id="sendTarget2" class="sendTarget2" name="sendTarget" value="每天" <#if '${(mail.sendTarget)!}'='每天'>selected</#if>>每天</option>
                    <option id="sendTarget3" class="sendTarget3" name="sendTarget" value="每星期" <#if '${(mail.sendTarget)!}'='每星期'>selected</#if>>每星期</option>
                    <option id="sendTarget4" class="sendTarget4" name="sendTarget" value="每月" <#if '${(mail.sendTarget)!}'='每月'>selected</#if>>每月</option>
                    <option id="sendTarget4" class="sendTarget4" name="sendTarget" value="每年" <#if '${(mail.sendTarget)!}'='每年'>selected</#if>>每年</option>
                </select>
            </div>
            <div class="layui-input-inline" style="width: 320px">
                <input type="text" class="layui-input sendTime" name="sendTime" id="sendTime" <#--lay-verify="required"--> value="${(mail.sendTime)!}" placeholder="发送时间:月/星期填数字；时间填hh:mm:ss;多个用;隔开">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">报表样式*</label>
            <div class="layui-input-inline" style="width: 150px;">
                <select id="selectTableStyle" name="selectTableStyle" lay-verify="required" lay-event="tableStyleSelectId">
                    <option id="tableStyle1" class="tableStyle1" name="tableStyle" value="">请选择报表样式</option>
                    <option id="tableStyle2" class="tableStyle2" name="tableStyle" value="yyyyMM" <#if '${(mail.tableStyle?substring(0,mail.tableStyle?index_of(";")))!}' = 'yyyyMM'>selected</#if>>yyyyMM</option>
                    <option id="tableStyle3" class="tableStyle3" name="tableStyle" value="yyyyMMdd" <#if '${(mail.tableStyle?substring(0,mail.tableStyle?index_of(";")))!}' = 'yyyyMMdd'>selected</#if>>yyyyMMdd</option>
                    <option id="tableStyle4" class="tableStyle4" name="tableStyle" value="yyyyMMddHH" <#if '${(mail.tableStyle?substring(0,mail.tableStyle?index_of(";")))!}' = 'yyyyMMddHH'>selected</#if>>yyyyMMddHH</option>
                    <option id="tableStyle5" class="tableStyle5" name="tableStyle" value="yyyyMMddHHmm" <#if '${(mail.tableStyle?substring(0,mail.tableStyle?index_of(";")))!}' = 'yyyyMMddHHmm'>selected</#if>>yyyyMMddHHmm</option>
                    <option id="tableStyle6" class="tableStyle6" name="tableStyle" value="yyyyMMddHHmmss" <#if '${(mail.tableStyle?substring(0,mail.tableStyle?index_of(";")))!}' = 'yyyyMMddHHmmss'>selected</#if>>yyyyMMddHHmmss</option>
                    <option id="tableStyle7" class="tableStyle7" name="tableStyle" value="MMddyyyy" <#if '${(mail.tableStyle?substring(0,mail.tableStyle?index_of(";")))!}' = 'MMddyyyy'>selected</#if>>MMddyyyy</option>
                    <option id="tableStyle7" class="tableStyle7" name="tableStyle" value="ddMMyyyy" <#if '${(mail.tableStyle?substring(0,mail.tableStyle?index_of(";")))!}' = 'MMddyyyy'>selected</#if>>MMddyyyy</option>
                </select>
            </div>
            <div class="layui-input-inline" style="width: 170px;">
                <input type="text" class="layui-input tableStyleDate" lay-verify="required" name="tableStyleDate" id="tableStyleDate" value="${(mail.tableStyle?substring(mail.tableStyle?index_of(";")+1))!}" placeholder="格式:需要-/+天,用;隔开">
            </div>
        </div>
    </div>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline" style="width: 100%">
            <label class="layui-form-label">导出列</label>
            <div class="layui-input-inline" style="width: 90%">
                <input type="text" class="layui-input outColumn" name="outColumn" id="outColumn" value="${(mail.outColumn)!}" placeholder="请输入导出列,用,隔开">
            </div>
        </div>
    </div>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline" style="width: 100%;">
            <label class="layui-form-label">主旨*</label>
            <div class="layui-input-inline" style="width: 90%;">
                <input type="text" class="layui-input subject" lay-verify="required" name="subject" id="subject" value="${(mail.subject)!}" placeholder="请输入主旨">
            </div>
        </div>
    </div>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline" style="width: 100%;">
            <label class="layui-form-label">收件者</label>
            <div class="layui-input-inline" style="width: 90%;">
                <textarea placeholder="请输入收件者" class="layui-textarea receiver" name="receiver" id="receiver">${(mail.receiver)!}</textarea>
            </div>
        </div>
    </div>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline" style="width: 100%;">
            <label class="layui-form-label">抄送</label>
            <div class="layui-input-inline" style="width: 90%;">
                <textarea placeholder="请输入抄送者" class="layui-textarea cc" name="cc" id="cc">${(mail.cc)!}</textarea>
            </div>
        </div>
    </div>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline" style="width: 100%;">
            <label class="layui-form-label">秘密抄送</label>
            <div class="layui-input-inline" style="width: 90%;">
                <textarea placeholder="请输入秘密抄送者" class="layui-textarea bcc" name="bcc" id="bcc">${(mail.bcc)!}</textarea>
            </div>
        </div>

    </div>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline" style="width: 100%;">
            <label class="layui-form-label">主题内容</label>
            <div class="layui-input-inline" style="width: 90%;">
                <textarea placeholder="请输入主体内容" class="layui-textarea body" name="body" id="body">${(mail.body)!}</textarea>
            </div>
        </div>
    </div>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline">
            <label class="layui-form-label">是否FTP上传*</label>
            <div class="layui-input-inline" style="width: 70px;">
                <input type="checkbox" id="isFtpUpload" name="isFtpUpload" lay-skin="switch" value="${(mail.isFtpUpload?string("true","false"))!}" lay-filter="isFtpUpload" lay-text="是|否" <#if '${(mail.isFtpUpload?string("true","false"))!}'='true'>checked</#if>>
            </div>
        </div>
        <div class="layui-inline" id="hiddenFtpServer" style="visibility: hidden">
            <label class="layui-form-label">FTP服务器*</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input ftpServer" name="ftpServer" id="ftpServer" value="${(mail.ftpServer)!}" placeholder="请输入FTP服务器名">
            </div>
        </div>
        <div class="layui-inline" id="hiddenFtpPort" style="visibility: hidden">
            <label class="layui-form-label">FTP端口*</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input ftpPort" name="ftpPort" id="ftpPort" value="${(mail.ftpPort)!}" placeholder="请输入FTP端口号">
            </div>
        </div>
        <div class="layui-inline" id="hiddenUserId" style="visibility: hidden">
            <label class="layui-form-label">FTP用户名*</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input ftpUserId" name="ftpUserId" id="ftpUserId" value="${(mail.ftpUserId)!}" placeholder="请输入FTP用户名">
            </div>
        </div>
        <div class="layui-inline" id="hiddenUserPwd" style="visibility: hidden">
            <label class="layui-form-label">FTP密码*</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input ftpUserPwd" name="ftpUserPwd" id="ftpUserPwd" value="${(mail.ftpUserPwd)!}" placeholder="请输入FTP密码">
            </div>
        </div>
        <div class="layui-inline" id="hiddenTableSuffix" style="visibility: hidden">
            <label class="layui-form-label">表后缀</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input tableSuffix" name="tableSuffix" id="tableSuffix" value="${(mail.tableSuffix)!}" placeholder="请输入表后缀">
            </div>
        </div>
        <div class="layui-inline" id="hiddenLocalPath" style="visibility: hidden">
            <label class="layui-form-label">FTP本地路径*</label>
            <div class="layui-input-inline" style="width: 370px;">
                <input type="text" class="layui-input ftpLocalPath" name="ftpLocalPath" id="ftpLocalPath" value="${(mail.ftpLocalPath)!}" placeholder="请输入FTP本地路径">
            </div>
        </div>
    </div>
    <br/>
    <br/>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-input-block" style="text-align: center;">
            <button class="layui-btn layui-btn-primary layui-border-blue layui-btn-lg" lay-submit=""
                    lay-filter="addOrUpdateMail">确认
            </button>
            <a class="layui-btn layui-btn-primary layui-border-blue layui-btn-lg"  id="closeDlg" href="javascript:void(0)">取消</a>
        </div>
    </div>
</form>
<script type="text/javascript" src="${ctx.contextPath}/js/mail/add.update.js"></script>
</body>
</html>