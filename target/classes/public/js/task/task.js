layui.use(['table', 'layer', "form"], function () {
    var layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        form = layui.form,
        table = layui.table;
    //邮箱信息列表展示
    var tableIns = table.render({
        elem: '#taskList',
        tableRow: true,
        url: ctx + '/task/list',
        cellMinWidth: 95,
        page: true,
        height: "full-50",
        limits: [10, 15, 20, 25],
        limit: 20,
        toolbar: "#toolbarDemo",
        id: "taskListTable",
        title: "task",
        cols: [[
            {type: "checkbox", fixed: "left", width: 50},
            {field: "id", title: "编号", fixed: "true", width: 58},
            {field: "taskType", title: "任务类型", fixed: "true", width: 58,templet: function (datas){
                if (datas.taskType == true) {
                    return "定时启动进程";
                } else {
                    return "定时关闭进程";
                }
            }},
            {field: "filePath", title: "文件路径", fixed: "true", width: 390},
            {field: "period", title: "任务周期", fixed: "true", width: 58},
            {field: "timeUnit", title: "时间单位", fixed: "true", width: 58, templet: function (datas){
                if (datas.timeUnit == 'second') {
                    return "秒";
                } else if (datas.timeUnit == 'minute') {
                    return "分";
                } else if (datas.timeUnit == 'hour') {
                    return "时";
                } else if (datas.timeUnit == 'day') {
                    return "天";
                }
            }},
            {field: "taskDatetime", title: "任务时间", fixed: "true", width: 58},
            {field: "pcName", title: "电脑名称", fixed: "true", width: 164},
            {field: "isDel", title: "是否启用", fixed: "true", width: 58, templet: function (datas){
                if (datas.isDel == 1) {
                    return "是";
                } else {
                    return "否";
                }
            }},
            {field: "createUser", title: "创建者", fixed: "true", width: 58},
            {field: "createDate", title: "创建日期", fixed: "true", width: 160},
            {field: "updateDate", title: "修改日期", fixed: "true", width: 160},
            {title: "操作", width: 150, templet: '#taskListBar', fixed: "right", align: "center"}
        ]]
    });

    //头工具栏事件
    table.on('toolbar(tasks)', function (obj) {
        var checkStatus = table.checkStatus(obj.config.id);
        switch (obj.event) {
            case "add":
                openAddOrUpdateDialog();
                break;
            case "del":
                delLine(checkStatus.data);
                break;
            case "timingStartAll":
                timingStartAll(checkStatus.data);
                break;
            case "timingStopAll":
                timingStopAll(checkStatus.data);
                break;
        };
    });

    /**
     * 行监听,users是lay-filter的值
     */
    table.on("tool(tasks)", function (obj) {
        var layEvent = obj.event;
        if (layEvent === "edit") {
            openAddOrUpdateDialog(obj.data.id);
        } else if (layEvent === "del") {
            layer.confirm("请确认删除此条数据？", {icon: 3, title: "任务管理"}, function (index) {
                $.post(ctx + "/task/delete", {ids: obj.data.id}, function (data) {
                    if (data.code == 200) {
                        layer.msg(data.message, {icon: 1});
                        tableIns.reload();
                    } else {
                        layer.msg(data.message, {icon: 5});
                    }
                });
            })
        }
    });

    /**
     * 监听单元格编辑事件
     * 表头设置edit属性，单元格编辑类型(默认不开启)，目前只支持：text(输入框)
     */
    table.on('edit(tasks)', function (obj) {
        //获取修改后的值
        //var value = obj.value;
        //得到当前修改的行的所有键值
        var data = obj.data;
        console.log(data)
        //得到修改的字段名
        //var postValue = obj.field;
        var event = {
            id: data.id
        };
        var url = ctx + "/task/update";
        $.post(url, event, function (res) {
            if (res.code == 200) {
                setTimeout(function () {
                    //top.layer.close(index);
                    top.layer.msg(res.message,{icon: 1});
                    layer.closeAll("iframe");
                    //刷新父页面
                    parent.location.reload();
                }, 500);
            } else {
                layer.msg(
                    res.message, {
                        icon: 5
                    }
                );
            }
        });
        return false;
    });

    function openAddOrUpdateDialog(uid) {
        var url = ctx + "/task/addOrUpdatePage";
        var title = "任务管理-添加";
        if (uid) {
            url = url + "?id=" + uid;
            title = "任务管理-修改";
        }
        var toIndex = layui.layer.open({
            title: title,
            type: 2,
            area: ["541px", "572px"],
            maxmin: true,
            content: url
        });
        //弹出层最大化
        // layer.full(toIndex);
    }

    /**
     * 批量删除
     * @param datas
     */
    function delLine(datas) {
        if (datas.length == 0) {
            layer.msg("请选择数据", {icon: 5});
            return;
        }

        layer.confirm("删除数据？", {
            btn: ["确认", "取消"] //按钮
        }, function (index) {
            layer.close(index);
            var ids = "ids=";
            for (var i = 0; i < datas.length; i++) {
                if (i < datas.length - 1) {
                    ids = ids + datas[i].id + "&ids=";
                } else {
                    ids = ids + datas[i].id
                }
            }
            $.ajax({
                type: "post",
                url: ctx + "/task/delete",
                data: ids,
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.message, {icon: 1});
                        tableIns.reload();
                    } else {
                        layer.msg(data.message, {icon: 5});
                    }
                }
            })
        });
    }

    function timingStartAll(checkData){
        if (checkData.length == 0) {
            layer.msg("请选择数据！",{icon: 5});
        }
        var ids = "ids=";
        for (var i = 0; i < checkData.length; i++) {
            if (i < checkData.length - 1) {
                ids = ids + checkData[i].id + "&ids=";
            } else {
                ids = ids + checkData[i].id
            }
        }
        console.log('id:', ids);
        $.ajax({
            type: "post",
            url: ctx + "/task/timingStartAll",
            data: ids,
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.message, {icon: 1});
                    tableIns.reload();
                } else {
                    layer.msg(data.message, {icon: 5});
                }
            }
        })
    }
    function timingStopAll(checkData){
        if (checkData.length == 0) {
            layer.msg("请选择数据！",{icon: 5});
        }
        var ids = "ids=";
        for (var i = 0; i < checkData.length; i++) {
            if (i < checkData.length - 1) {
                ids = ids + checkData[i].id + "&ids=";
            } else {
                ids = ids + checkData[i].id
            }
        }
        $.ajax({
            type: "post",
            url: ctx + "/task/timingStopAll",
            data: ids,
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.message, {icon: 1});
                    tableIns.reload();
                } else {
                    layer.msg(data.message, {icon: 5});
                }
            }
        })
    }
});
