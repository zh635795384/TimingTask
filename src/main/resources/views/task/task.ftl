<!DOCTYPE html>
<html>
<#include "../common.ftl">
<head>
</head>
<body class="childrenBody">
<form class="layui-form" lay-filter="filter-form">
    <table id="taskList" class="layui-table" lay-filter="tasks"></table>
    <script type="text/html" id="toolbarDemo">
        <div class="layui-input-inline">
            <div class="layui-btn-container">
                <a class="layui-btn layui-btn-primary layui-border-blue layui-btn-sm addNews_btn" lay-event="add"><i class="layui-icon">&#xe608;</i>添加</a>
            </div>
        </div>
        <div class="layui-input-inline">
            <div class="layui-btn-container">
                <a class="layui-btn layui-btn-primary layui-border-red layui-btn-sm delNews_btn" lay-event="del"><i class="layui-icon">&#xe608;</i>删除</a>
            </div>
        </div>
        <div class="layui-input-inline">
            <div class="layui-btn-container">
                <a class="layui-btn layui-btn-primary layui-border-blue layui-btn-sm" lay-event="timingStartAll"><i class="layui-icon">&#xe652;</i>全部开始</a>
            </div>
        </div>
        <div class="layui-input-inline">
            <div class="layui-btn-container">
                <a class="layui-btn layui-btn-primary layui-border-red layui-btn-sm" lay-event="timingStopAll"><i class="layui-icon">&#xe651;</i>全部停止</a>
            </div>
        </div>
    </script>
    <script id="taskListBar" type="text/html">
        <a class="layui-btn layui-btn-primary layui-border-blue layui-btn-xs" id="edit" lay-event="edit">编辑</a>
        <a class="layui-btn layui-btn-primary layui-border-red layui-btn-xs" lay-event="del">删除</a>
    </script>
</form>
<script type="text/javascript" src="${ctx.contextPath}/js/task/task.js"></script>
</body>
</html>