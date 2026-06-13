<!DOCTYPE html>
<html>
<#include "../common.ftl">
<head>
    <meta charset="utf-8">
    <title>邮件分组收件配置</title>
    <script>
        // ftl页面内渲染json，挂载全局变量，外部js可用
        var globalMailReceiver = ${mailReceiver};
        var globalMail = ${mail};
    </script>
</head>
<body class="layui-padding-20">
<div class="layui-card">
    <div class="layui-card-header">
        <button class="layui-btn layui-btn-sm layui-btn-success" id="addRow">+ 新增配置行</button>
        <button class="layui-btn layui-btn-sm layui-btn-normal" id="saveAll">保存全部配置</button>
        <button class="layui-btn layui-btn-sm layui-btn-warm" id="reset">重置</button>
        <span class="layui-text layui-font-12">说明：多个邮箱使用【英文逗号;】分隔</span>
    </div>
    <div class="layui-card-body">
        <table class="layui-table">
            <thead>
            <tr>
                <th>分组编码(group_key)</th>
                <th>收件人(receiver)</th>
                <th>抄送(cc)</th>
                <th>密送(bcc)</th>
                <th width="90">操作</th>
            </tr>
            </thead>
            <tbody id="tableBody"></tbody>
        </table>
    </div>
</div>
<script type="text/javascript" src="${ctx.contextPath}/js/mail/receiver.js"></script>
</body>
</html>