<!DOCTYPE html>
<html>
<#include "../common.ftl">
<head>
</head>
<body class="childrenBody">
<form class="layui-form" style="width:100%;">
    <input name="id" type="hidden" value="${(task.id)!}"/>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-inline">
            <label class="layui-form-label">任务类型<sup>*</sup></label>
            <div class="layui-input-inline">
                <input type="checkbox" name="taskType" id="taskType" value="{{d.taskType}}" lay-skin="switch" lay-text="定时启动进程|定时关闭进程" lay-filter="taskTypeSwitch" {{#  if(d.taskType == 1){ }} checked {{#  } }}>
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">文件路径<sup>*</sup></label>
            <div class="layui-input-inline">
<#--                <input type="file" name="filePath" id="filePath">-->
                <input type="text" class="layui-input filePath" name="filePath" id="filePath" lay-verify="required" value="${(task.filePath)!}" placeholder="请输入文件路径">
<#--                <button type="button" class="layui-btn layui-btn-normal" id="chooseFile">选择文件</button>-->
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">任务周期<sup>*</sup></label>
            <div class="layui-input-inline">
                <select id="period" name="period" lay-verify="required" lay-event="periodSelectId">
                    <option id="period1" class="period1" name="period" value="">请选择任务周期</option>
                    <option id="period2" class="period2" name="period" value="每天" <#if '${(task.period)!}'='每天'>selected</#if>>每天</option>
                    <option id="period3" class="period3" name="period" value="每星期" <#if '${(task.period)!}'='每星期'>selected</#if>>每星期</option>
                    <option id="period4" class="period4" name="period" value="每月" <#if '${(task.period)!}'='每月'>selected</#if>>每月</option>
                    <option id="period4" class="period4" name="period" value="每年" <#if '${(task.period)!}'='每年'>selected</#if>>每年</option>
                </select>
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">间隔时间<sup>*</sup></label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input taskTime" name="taskTime" id="taskTime" lay-verify="required" value="${(task.taskTime)!}" placeholder="请输入间隔时间">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">时间单位<sup>*</sup></label>
            <div class="layui-input-inline">
                <select id="timeUnit" name="timeUnit" lay-verify="required" lay-event="timeUnitSelectId" lay-verify="required">
                    <option id="timeUnit1" class="timeUnit1" name="period" value="">请选择时间单位</option>
                    <option id="timeUnit2" class="timeUnit2" name="period" value="second" <#if '${(task.timeUnit)!}'='second'>selected</#if>>秒</option>
                    <option id="timeUnit3" class="timeUnit3" name="period" value="minute" <#if '${(task.timeUnit)!}'='minute'>selected</#if>>分</option>
                    <option id="timeUnit4" class="timeUnit4" name="period" value="hour" <#if '${(task.timeUnit)!}'='hour'>selected</#if>>时</option>
                    <option id="timeUnit4" class="timeUnit4" name="period" value="day" <#if '${(task.timeUnit)!}'='day'>selected</#if>>天</option>
                </select>
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">任务开始时间</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input startDatetime" name="startDatetime" id="startDatetime" value="${(task.startDatetime)!}" placeholder="请选择任务开始时间">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">任务结束时间</label>
            <div class="layui-input-inline">
                <input type="text" class="layui-input endDatetime" name="endDatetime" id="endDatetime" value="${(task.endDatetime)!}" placeholder="请选择任务结束时间">
            </div>
        </div>
        <div class="layui-inline">
            <label class="layui-form-label">是否启用<sup>*</sup></label>
            <div class="layui-input-inline">
                <input type="checkbox" name="isDel" id="isDel" value="{{d.isDel}}" lay-skin="switch" lay-text="是|否" lay-filter="isDelSwitch" {{#  if(d.isDel == 1){ }} checked {{#  } }}>
            </div>
        </div>
    </div>
    <br/>
    <br/>
    <div class="layui-form-item layui-row layui-col-xs12">
        <div class="layui-input-block" style="text-align: center;">
            <button class="layui-btn layui-btn-primary layui-border-blue layui-btn-lg" lay-submit=""
                    lay-filter="addOrUpdateTask">确认
            </button>
            <a class="layui-btn layui-btn-primary layui-border-blue layui-btn-lg"  id="closeDlg" href="javascript:void(0)">取消</a>
        </div>
    </div>
</form>
<script type="text/javascript" src="${ctx.contextPath}/js/task/add.update.js"></script>
</body>
</html>