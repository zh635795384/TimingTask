<!DOCTYPE html>
<html>
<#include "common.ftl">
<head>
    <meta charset="utf-8">
    <title>定时任务管理</title>
</head>
<body class="layui-layout-body layuimini-all">
<div class="layui-layout layui-layout-admin">
    <div class="layui-header header">
        <div class="layui-logo" align="left">
            <a href=""><h1 style="margin: 0 0 0 3px">定时任务</h1></a>
        </div>
        <a>
            <div class="layuimini-tool"><i title="展开" class="fa fa-outdent" data-side-fold="1"></i></div>
        </a>
        <ul class="layui-nav layui-layout-right">
            <li class="layui-nav-item mobile layui-hide-xs" lay-unselect>
                <a href="javascript:;" data-check-screen="full"><i class="fa fa-arrows-alt"></i></a>
            </li>
            <li class="layui-nav-item mobile layui-hide-xs">
                <a href="javascript:;" data-iframe-tab="${ctx.contextPath}/user/message" data-title="消息">
                    <i class="fa fa-bell"><span class="layui-badge" style="padding: 0 3px;border-radius: 9px;margin-left: 2px;">${(messageCount)!'0'}</span></i>
                </a>
            </li>
            <li class="layui-nav-item layuimini-setting">
                <a href="javascript:;">admin</a>
            </li>
            <li class="layui-nav-item layuimini-select-bgcolor mobile layui-hide-xs" lay-unselect><a href="javascript:;"></a></li>
        </ul>
    </div>
    <div class="layui-side layui-bg-black">
        <div class="layui-side-scroll layui-left-menu">
            <ul class="layui-nav layui-nav-tree layui-left-nav-tree layui-this" id="currency">
                <li class="layui-nav-item">
                    <a href="javascript:;" class="layui-menu-tips">
                        <i class="fa fa-codepen"></i><span class="layui-left-nav">邮箱定时任务管理</span><span class="layui-nav-more"></span>
                    </a>
                    <dl class="layui-nav-child">
                        <dd class="">
                            <a href="javascript:;" class="layui-menu-tips" data-type="tabAdd" data-tab-mpi="m-p-i-12" data-tab="task/index" target="_self"><i class="fa fa-balance-scale"></i><span class="layui-left-nav">定时任务管理</span></a>
                        </dd>
                        <dd class="">
                            <a href="javascript:;" class="layui-menu-tips" data-type="tabAdd" data-tab-mpi="m-p-i-12" data-tab="mail/index" target="_self"><i class="fa fa-balance-scale"></i><span class="layui-left-nav">邮箱管理</span></a>
                        </dd>
                    </dl>
                </li>
                <span class="layui-nav-bar" style="top: 201px; height: 0px; opacity: 0;"></span>
            </ul>
        </div>
    </div>
    <div class="layui-body">
        <div class="layui-tab" lay-filter="layuiminiTab" id="top_tabs_box">
            <ul class="layui-tab-title" id="top_tabs">
                <li class="layui-this" id="layuiminiHomeTabId" lay-id="welcome">
                    <i class="fa fa-home"></i><span>首页</span>
                </li>
            </ul>
            <ul class="layui-nav closeBox">
                <li class="layui-nav-item">
                    <a href="javascript:;"><i class="fa fa-dot-circle-o"></i>页面操作</a>
                    <dl class="layui-nav-child">
                        <dd><a href="javascript:;" data-page-close="other"><i class="fa fa-window-close"></i>关闭其他</a></dd>
                        <dd><a href="javascript:;" data-page-close="all"><i class="fa fa-window-close-o"></i>关闭所有</a></dd>
                    </dl>
                </li>
            </ul>
            <div class="layui-tab-content clildFrame">
                <div id="layuiminiHomeTabIframe" class="layui-tab-item layui-show">
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript" src="${ctx.contextPath}/js/index.js"></script>
</body>
</html>
