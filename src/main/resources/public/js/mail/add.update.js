layui.use(['form', 'layer'], function () {
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery;

    /**直接读取变量初始值**/
    var isExport = $('#isExport').prop('checked');
    var divExcel = $('#divExcel').prop('checked');
    var insertRow = $('#insertRow').prop('checked');
    var isRar = $('#isRar').prop('checked');
    var isFtpUpload = $('#isFtpUpload').prop('checked');
    var emptyAttach = $('#emptyAttach').prop('checked');

    isFtpUpload = $('#isFtpUpload').val();
    var hiddenFtpServer = document.getElementById('hiddenFtpServer');
    var hiddenFtpPort = document.getElementById('hiddenFtpPort');
    var hiddenUserId = document.getElementById('hiddenUserId');
    var hiddenUserPwd = document.getElementById('hiddenUserPwd');
    var hiddenLocalPath = document.getElementById('hiddenLocalPath');
    var hiddenTableSuffix = document.getElementById('hiddenTableSuffix');
    // console.log("初始状态:", isFtpUpload)
    if ((isFtpUpload || isFtpUpload == "true") && isFtpUpload != "false") {
        hiddenFtpServer.style.visibility = 'visible';
        hiddenFtpPort.style.visibility = 'visible';
        hiddenUserId.style.visibility = 'visible';
        hiddenUserPwd.style.visibility = 'visible';
        hiddenLocalPath.style.visibility = 'visible';
        hiddenTableSuffix.style.visibility = 'visible';
    } else {
        hiddenFtpServer.style.visibility= 'hidden';
        hiddenFtpPort.style.visibility= 'hidden';
        hiddenUserId.style.visibility= 'hidden';
        hiddenUserPwd.style.visibility= 'hidden';
        hiddenLocalPath.style.visibility = 'hidden';
        hiddenTableSuffix.style.visibility = 'hidden';
    }

    form.on("submit(addOrUpdateMail)", function (data) {
        index = top.layer.msg("数据正在提交...", {icon: 16, time: false, shade: 0.8});
        //弹出loading
        var selectTableStyle = $("#selectTableStyle").val();
        var tableStyleDate = $("#tableStyleDate").val();
        var url = ctx + "/mail/save";
        if ($("input[name='id']").val()) {
            url = ctx + "/mail/update";
        }
        // data.field.isExport = isExport != undefined && isExport != '' && isExport != null ? isExport :  false;
        data.field.isExport = !!(isExport && isExport !== 'false');
        // data.field.divExcel = divExcel != undefined && divExcel != '' && divExcel != null ? divExcel : false;
        data.field.divExcel = !!(divExcel && divExcel !== 'false');
        // data.field.insertRow = insertRow != undefined && insertRow != '' && insertRow != null ? insertRow : false;
        data.field.insertRow = !!(insertRow && insertRow !== 'false');
        // data.field.isRar = isRar != undefined && isRar != '' && isRar != null ? isRar : false;
        data.field.isRar = !!(isRar && isRar !== 'false');
        // data.field.isFtpUpload = isFtpUpload != undefined && isFtpUpload != '' && isFtpUpload != null ? isFtpUpload : false;
        data.field.isFtpUpload = !!(isFtpUpload && isFtpUpload !== 'false');
        data.field.emptyAttach = !!(emptyAttach && emptyAttach !== 'false');
        data.field.tableStyle = selectTableStyle + ";" + (tableStyleDate != null && tableStyleDate != '' && tableStyleDate != undefined ? tableStyleDate : tableStyleDate + ";");
        // console.log(data.field)
        if (data.field.isFtpUpload) {
            if (data.field.ftpServer == null || data.field.ftpServer === '') {
                layer.msg("请输入服务器名");
                return false;
            }
            if (data.field.ftpPort == null || data.field.ftpPort === '') {
                layer.msg("请输入端口号");
                return false;
            }
            if (data.field.ftpUserId == null || data.field.ftpUserId === '') {
                layer.msg("请输入用户名");
                return false;
            }
            if (data.field.ftpUserPwd == null || data.field.ftpUserPwd === '') {
                layer.msg("请输入密码");
                return false;
            }
            if (data.field.ftpLocalPath == null || data.field.ftpLocalPath === '') {
                layer.msg("请输入本地路径");
                return false;
            }
            if (data.field.path == null || data.field.path === '') {
                layer.msg("请输入导出路径");
                return false;
            }
        }
        $.post(url, data.field, function (res) {
            if (res.code == 200) {
                setTimeout(function () {
                    top.layer.close(index);
                    top.layer.msg(res.message, {icon: 1});
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

    form.on('switch(isExport)', function (data) {
        isExport = data.elem.checked;
     });

    form.on('switch(divExcel)', function (data) {
        divExcel = data.elem.checked;
    });

    form.on('switch(insertRow)', function (data) {
        insertRow = data.elem.checked;
    });

    form.on('switch(isRar)', function (data) {
        isRar = data.elem.checked;
    });
    form.on('switch(emptyAttach)', function (data) {
        emptyAttach = data.elem.checked;
    });

    form.on('switch(isFtpUpload)', function (data) {
        isFtpUpload = data.elem.checked;
        // console.log(isFtpUpload)
        if ((isFtpUpload || isFtpUpload == "true") && isFtpUpload != "false") {
            hiddenFtpServer.style.visibility = 'visible';
            hiddenFtpPort.style.visibility = 'visible';
            hiddenUserId.style.visibility = 'visible';
            hiddenUserPwd.style.visibility = 'visible';
            hiddenLocalPath.style.visibility = 'visible';
            hiddenTableSuffix.style.visibility = 'visible';
        } else {
            hiddenFtpServer.style.visibility= 'hidden';
            hiddenFtpPort.style.visibility= 'hidden';
            hiddenUserId.style.visibility= 'hidden';
            hiddenUserPwd.style.visibility= 'hidden';
            hiddenLocalPath.style.visibility = 'hidden';
            hiddenTableSuffix.style.visibility = 'hidden';
        }
    });

    $("#closeDlg").click(function () {
        // iframe 页面关闭 添加parent
        parent.layer.closeAll();
    })

});