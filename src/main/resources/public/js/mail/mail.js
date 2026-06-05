layui.use(['table', 'layer', 'element', "form",'tableFilter'], function () {
    var layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        form = layui.form,
        tableFilter = layui.tableFilter,
        table = layui.table;
    //邮箱信息列表展示
    var tableIns = table.render({
        elem: '#mailList',
        tableRow: true,
        url: ctx + '/mail/list',
        cellMinWidth: 50,
        page: true,
        height: "full-50",
        limits: [50, 100, 200, 500],
        limit: 50,
        toolbar: "#toolbarDemo",
        id: "mailListTable",
        title: "mail",
        cols: [[
            {type: "checkbox", fixed: "left", width: 50},
            {field: "id", title: "编号", width: 58, align: "center"},
            {field: 'tableName', title: "表名", width: 225, align: "center"},
            {field: 'tablePrefix', title: "别名", width: 86, align: "center", edit: 'text'},
            {field: 'tableSuffix', title: "后缀", width: 65, align: "center", edit: 'text'},
            {field: 'tableStyle', title: "表样式", width: 121, align: "center", edit: 'text'},
            {field: 'postPrefix', title: "提交后缀", width: 86, align: "center", edit: 'text'},
            {field: 'sendTarget', title: "发送周期", width: 86, align: "center", edit: 'text'},
            {field: 'sendTime', title: "发送时间", width: 86, align: "center", edit: 'text'},
            {field: 'isRar', title: "是否压缩", width: 86, align: "center",templet:(data)=>{
                    if (data.isRar == true) {
                        return '<span className="layui-badge layui-bg-blue">是</span>';
                    } else {
                        return '<span className="layui-badge layui-bg-blue">否</span>';
                    }
            }},
            {field: 'groupBy', title: "分组", width: 58, align: "center", edit: 'text'},
            {field: 'fileName', title: "导出附件名", width: 100, align: "center", edit: 'text'},
            {field: 'divExcel', title: "分sheet", width: 81, align: "center",templet:(data)=>{
                if (data.divExcel == true) {
                    return '<span className="layui-badge layui-bg-blue">是</span>';
                } else {
                    return '<span className="layui-badge layui-bg-blue">否</span>';
                }
            }},
            {field: 'outColumn', title: "导出字段", width: 86, align: "center", edit: 'text'},
            {field: 'fonts', title: "字体", width: 58, align: "center", edit: 'text'},
            {field: 'fontSize', title: "字体大小", width: 86, align: "center", edit: 'text'},
            {field: 'insertRow', title: "插入空白行", width: 100, align: "center", templet:(data)=>{
                    if (data.insertRow == true) {
                        return '<span className="layui-badge layui-bg-blue">是</span>';
                    } else {
                        return '<span className="layui-badge layui-bg-blue">否</span>';
                    }
            }},
            {field: 'path', title: "导出路径", width: 86, align: "center", edit: 'text'},
            {field: 'subject', title: "主旨", width: 58, align: "center", edit: 'text'},
            {field: 'sender', title: "发件者", width: 217, align: "center", edit: 'text'},
            // {field: 'receiver', title: "收件者", width: 217, align: "center", edit: 'text'},
            // {field: 'cc', title: "CC", width: 58, align: "center", edit: 'text'},
            // {field: 'bcc', title: "BCC", width: 72, align: "center", edit: 'text'},
            {field: 'attach', title: "附件", width: 58, align: "center", edit: 'text'},
            {field: 'body', title: "主体内容", width: 86, align: "center", edit: 'text'},
            {field: 'contact', title: "合并报表", width: 86, align: "center", edit: 'text'},
            {field: 'isExport', title: "是否只导出", width: 100, align: "center",templet:(data)=>{
                    if (data.isExport == true) {
                        return '<span className="layui-badge layui-bg-blue">是</span>';
                    } else {
                        return '<span className="layui-badge layui-bg-blue">否</span>';
                    }
            }},
            {field: 'isFtpUpload', title: "是否ftp上传", width: 100, align: "center",templet:(data)=>{
                    if (data.isFtpUpload == true) {
                        return '<span className="layui-badge layui-bg-blue">是</span>';
                    } else {
                        return '<span className="layui-badge layui-bg-blue">否</span>';
                    }
            }},
            {field: 'ftpServer', title: "FTP服务器", align: 'center', width: 103},
            {field: 'ftpPort', title: "FTP端口", align: 'center', width: 89},
            {field: 'ftpUserId', title: "FTP用户名", align: 'center', width: 89},
            {field: 'ftpUserPwd', title: "FTP密码", align: 'center', width: 89},
            {field: 'ftpLocalPath', title: "FTP本地路径", align: 'center', width: 226},
            {field: 'isDel', title: "是否删除", align: 'center', width: 86, templet: function (data){
                    if (data.isDel == 0){
                        return "否"
                    } else {
                        return "是"
                    }
                }},
            {field: 'createUser', title: "创建者", align: 'center', width: 72},
            {field: 'createDate', title: "创建时间", align: 'center', width: 86, templet: '<div>{{ layui.util.toDateString(d.createDate, "yyyy-MM-dd HH:mm:ss") }}</div>'},
            {field: 'updateDate', title: "修改时间", align: 'center', width: 86, templet: '<div>{{ layui.util.toDateString(d.updateDate, "yyyy-MM-dd HH:mm:ss") }}</div>'},
            {title: "操作", width: 150, templet: '#mailListBar', fixed: "right", align: "center"}
        ]]
    });

    //头工具栏事件
    table.on('toolbar(mails)', function (obj) {
        var checkStatus = table.checkStatus(obj.config.id);
        switch (obj.event) {
            case "add":
                openAddOrUpdateDialog();
                break;
            case "del":
                delLine(checkStatus.data);
                break;
        };
    });

    /**
     * 行监听,users是lay-filter的值
     */
    table.on("tool(mails)", function (obj) {
        var layEvent = obj.event;
        if (layEvent === "edit") {
            openAddOrUpdateDialog(obj.data.id);
        } else if (layEvent === "del") {
            layer.confirm("确定删除？", {icon: 3, title: "邮件管理"}, function (index) {
                $.post(ctx + "/mail/delete", {ids: obj.data.id}, function (data) {
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

    form.on('switch(runEmail)', function(obj){
        var isRun = obj.elem.checked;
        // isRun = obj.elem.checked == false ? false : true;
        console.log(isRun)
        $.ajax({
            type: "post",
            url: ctx + "/mail/runEmail",
            data: {run: isRun},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.message, {icon: 1});
                    // tableIns.reload();
                    this.checked = true;
                } else {
                    layer.msg(data.message, {icon: 5});
                }
            }
        })
    });

    /**
     * 监听单元格编辑事件
     * 表头设置edit属性，单元格编辑类型(默认不开启)，目前只支持：text(输入框)
     */
    table.on('edit(mails)', function (obj) {
        //获取修改后的值
        //var value = obj.value;
        //得到当前修改的行的所有键值
        var data = obj.data;
        var runEmail = $('#runEmail').prop('checked');
        //得到修改的字段名
        //var postValue = obj.field;
        var mail = {
            id: data.id, tableName: data.tableName, tablePrefix: data.tablePrefix, tableSuffix: data.tableSuffix, tableStyle: data.tableStyle,
            postPrefix: data.postPrefix, sendTarget: data.sendTarget,sendTime: data.sendTime, isRar: data.isRar, groupBy: data.groupBy,
            fileName: data.fileName, divExcel: data.divExcel, outColumn: data.outColumn, fonts: data.fonts, fontSize: data.fontSize,
            insertRow: data.insertRow, path: data.path, subject: data.subject, sender: data.sender, receiver: data.receiver, cc: data.cc,
            bcc: data.bcc, attach: data.attach, body: data.body, contact: data.contact,isExport: data.isExport, isFtpUpload: data.isFtpUpload,
            ftpServer: data.ftpServer, ftpPort: data.ftpPort, ftpUserId: data.ftpUserId, ftpUserPwd: data.ftpUserPwd, ftpLocalPath: data.ftpLocalPath,
            isDel: data.isDel
        };
        var url = ctx + "/mail/update?isRun="+runEmail;
        $.post(url, mail, function (res) {
            if (res.code == 200) {
                tableIns.reloadData('mailListTable',{});
                // $('#runEmail').prop('checked') = runEmail;
                /*setTimeout(function () {
                    //top.layer.close(index);
                    top.layer.msg(res.message,{icon: 1});
                    layer.closeAll("iframe");
                    //刷新父页面
                    parent.location.reload();
                }, 500);*/
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
        var url = ctx + "/mail/addOrUpdatePage";
        var title = "邮件管理-添加";
        if (uid) {
            url = url + "?id=" + uid;
            title = "邮件管理-更新";
        }
        var toIndex = layui.layer.open({
            title: title,
            type: 2,
            area: ["100%", "100%"],
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

        layer.confirm("确认删除？", {
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
                url: ctx + "/mail/delete",
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

    var tableFilterIns = tableFilter.render({
        'elem' : '#mailList',//table的选择器
        'mode' : 'local',//过滤模式
        'filters' : [
            {field: 'isDel', type:'checkbox'}
        ],//过滤项配置
        'done': function(filters){
            //结果回调
            console.log(filters)
        }
    })
});
