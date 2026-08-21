layui.use(['jquery','layer'], function(){
    var $ = layui.jquery;
    // 直接拿页面预定义全局对象
    var mailReceiver = globalMailReceiver;
    var mail = globalMail;

    // console.table(mail);

    var mailId = mail != null ? mail.id : null;
    console.log("mail",mail)

    $(function(){
        // 页面加载执行查询
        if(mailId){
            loadConfig();
        }else{
            layer.msg("未获取到邮件ID");
            $('#tableBody').html(buildTr('','','','',''));
        }
    })

    // 邮箱正则：匹配单个合法邮箱
    function isEmail(str) {
        let reg = /^[A-Za-z0-9._-]+@[A-Za-z0-9_-]+(\.[A-Za-z0-9_-]+)*$/;
        return reg.test(str.trim());
        /*let s = str.trim();
        let atIdx = s.indexOf('@');
        let dotIdx = s.lastIndexOf('.');
        // 简单判断：@ 不在首位，. 在 @ 后面，. 不在末尾
        return atIdx > 0 && dotIdx > atIdx && dotIdx < s.length - 1;*/
    }

    // 校验整串邮箱（英文逗号分隔多个）
    function checkEmailStr(emailStr) {
        if (!emailStr) return true; // 空字符串不校验（抄送/密送选填）
        let emailArr = emailStr.split(';');
        for (let e of emailArr) {
            let val = e.trim();
            if (val && !isEmail(val)) {
                return false;
            }
        }
        return true;
    }
    // 拉取后台分组数据回显
    function loadConfig(){
        $.get(ctx + "/mail/receiverList",{id: mailId},function(res){
            let html = '';
            console.log(res)
            if(res && res.length>0){
                res.forEach(item=>{
                    html += buildTr(item.id,item.groupKey, item.receiver, item.cc, item.bcc)
                })
            }else{
                html = buildTr('','','','','')
            }
            $('#tableBody').html(html);
        },'json')
    }

    //生成单行模板（这里是JS字符串模板，不要再写${gk}！！之前错误点，改为可拉伸文本域）
    function buildTr(id,gk,recv,cc,bcc){
        id = id || '';
        gk = gk || '';
        recv = recv || '';
        cc = cc || '';
        bcc = bcc || '';
        return '<tr data-id="' + id + '">'
            + '<td><input class="layui-input group_key" value="' + gk + '" placeholder="如：BJ、SH"></td>'
            + '<td><textarea class="layui-textarea receiver" style="min-height:60px;resize:vertical;" placeholder="a@xx.com;b@xx.com">' + recv + '</textarea></td>'
            + '<td><textarea class="layui-textarea cc" style="min-height:60px;resize:vertical;" placeholder="选填">' + cc + '</textarea></td>'
            + '<td><textarea class="layui-textarea bcc" style="min-height:60px;resize:vertical;" placeholder="选填">' + bcc + '</textarea></td>'
            + '<td><button class="layui-btn layui-btn-xs layui-btn-danger del">删除</button></td>'
            + '</tr>';
    }

    //新增行
    $('#addRow').click(()=>{
        $('#tableBody').append(buildTr('','','','',''))
    })
    //删除行
    $('body').on('click','.del',function(){
        let $tr = $(this).parents('tr');
        let id = $tr.data('id');
        // 没有ID=新增未保存的空行，只删前端DOM即可
        if(!id){
            $tr.remove();
            return;
        }
        // 确认弹窗
        layer.confirm('确定要删除这条收件配置吗？', {icon: 3, title:'提示'}, function(index){
            // 请求后台删除接口 deleteReceiver
            $.post(ctx + "/mail/deleteReceiver", {id: id}, function(res){
                if(res.code === 200){
                    layer.msg('删除成功',{icon:1});
                    $tr.remove(); // 后端删成功再移除前端行
                }else{
                    layer.msg(res.msg || '删除失败',{icon:5});
                }
            },'json');
            layer.close(index);
        });
    })
    //重置：重新从数据库拉原始数据
    $('#reset').click(()=>loadConfig())

    //保存批量配置
    $('#saveAll').click(function(){
        let saveList = [];
        let checkOk = true;
        $('#tableBody tr').each(function(){
            let id = $(this).data('id');
            let gk = $(this).find('.group_key').val().trim();
            let recv = $(this).find('.receiver').val().trim();
            let cc = $(this).find('.cc').val().trim();
            let bcc = $(this).find('.bcc').val().trim();
            let groupBy = mail.groupBy == undefined || mail.groupBy == '' ? false : true;
            console.log("groupBy",groupBy);
            // 1. 分组编码非空校验
            if(!gk && groupBy && !mail.divExcel){
                layer.msg('分组编码不能为空');
                checkOk = false;
                return false;
            }
            // 2. 收件人非空 + 邮箱格式校验（必填）
            if(!recv){
                layer.msg('收件邮箱不能为空');
                checkOk = false;
                return false;
            }
            if(!checkEmailStr(recv)){
                layer.msg('收件邮箱格式错误，请检查（多个邮箱用英文;分隔）');
                checkOk = false;
                return false;
            }
            // 3. 抄送邮箱格式校验（选填，有值才校验）
            if(!checkEmailStr(cc)){
                layer.msg('抄送邮箱格式错误，请检查（多个邮箱用英文;分隔）');
                checkOk = false;
                return false;
            }
            // 4. 密送邮箱格式校验（选填，有值才校验）
            if(!checkEmailStr(bcc)){
                layer.msg('密送邮箱格式错误，请检查（多个邮箱用英文;分隔）');
                checkOk = false;
                return false;
            }
            saveList.push({id:id,mailId:mailId,groupKey:gk,receiver:recv,cc:cc,bcc:bcc})
        })
        if(!checkOk || saveList.length===0) return;
        // console.log("传给后台",saveList)
        $.ajax({
            url:ctx + "/mail/saveReceiver",
            // contentType:'application/json',
            data:{mailReceiver: JSON.stringify(saveList), mail: JSON.stringify(mail)},
            type:'post',
            success:function(res){
                if(res.code===200){
                    layer.msg('保存成功',{icon:1});
                    loadConfig();
                    if(parent.tableMail){
                        parent.tableMail.reload();
                    }
                    // 关闭当前弹出层（若为 layui layer 的 iframe 页）
                    var index = parent.layer.getFrameIndex(window.name);
                    parent.layer.close(index);
                    // setTimeout(()=>parent.layer.closeAll(),400)
                }else{
                    layer.msg(res.msg||'保存失败')
                }
            }
        })
    })
})