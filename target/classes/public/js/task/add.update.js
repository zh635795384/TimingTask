layui.use(['form', 'layer'], function () {
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        laydate = layui.laydate,
        $ = layui.jquery;

    // var taskType = false,isDel = false;

    laydate.render({
        elem: '#startDatetime',
        type: 'datetime',
        fullPanel: true // 2.8+
    });

    laydate.render({
        elem: '#endDatetime',
        type: 'datetime',
        fullPanel: true // 2.8+
    });

    form.on("submit(addOrUpdateTask)", function (data) {
        index = top.layer.msg("数据提交中。。。", {icon: 16, time: false, shade: 0.8});
        //弹出loading
        var url = ctx + "/task/save";
        if ($("input[name='id']").val()) {
            url = ctx + "/task/update";
        }
        data.field.taskType = $('#taskType').prop('checked');
        data.field.isDel = $('#isDel').prop('checked');
        data.field.startDatetime = layui.util.toDateString(data.field.startDatetime, 'yyyy-MM-dd HH:mm:ss');
        data.field.endDatetime = layui.util.toDateString(data.field.endDatetime, 'yyyy-MM-dd HH:mm:ss');
        console.log(data.field);
        if (data.field.filePath == null || data.field.filePath === '') {
            layer.msg("请输入文件全路径！");
            return false;
        }
        if (data.field.period == null || data.field.period === '') {
            layer.msg("请选择周期！");
            return false;
        }
        if (data.field.taskTime == null || data.field.taskTime === '') {
            layer.msg("请输入间隔时间！");
            return false;
        }
        if (data.field.timeUnit == null || data.field.timeUnit === '') {
            layer.msg("请选择时间单位！");
            return false;
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

    /**
     * switch有bug，传不到后端值，等待解决
     */
    /*form.on('switch(isDelSwitch)', function (data) {
        // console.log(this.checked)
        isDel = this.checked;
    });*/

    /*form.on('switch(taskTypeSwitch)', function(obj){
        if (this.checked == false){
            taskType = false;
        } else {
            taskType = true;
        }
    });*/

    $("#closeDlg").click(function () {
        // iframe 页面关闭 添加parent
        parent.layer.closeAll();
    })
});