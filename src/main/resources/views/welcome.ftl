<!DOCTYPE html>
<html>
<head>
  <#include "common.ftl">
    <style>
        .indexTable {
            background-color: #FFFFFF;
        }
        .h1 {
            margin-top: 20%;
            align-content: center;
        }
        .h1Text {
            align-content: center;
            font-size: xxx-large;
            font-family: '楷体', '微软雅黑';
            height: 90%;
        }
    </style>
</head>
<body class="childrenBody">

<div class="layui-tab-item layui-show">
    <div class="layui-carousel" id="test10" align="center">
        <div carousel-item="">
            <div class="indexTable">
                <div class="h1">
                    <h1 class="h1Text">欢迎</h1>
                </div>
                <#--<img src="${ctx.contextPath}/images/index.png">-->
            </div>
        </div>
    </div>
</div>

<script>
    layui.use(['carousel', 'form'], function(){
        var carousel = layui.carousel
            ,form = layui.form;

        //常规轮播
        carousel.render({
            elem: '#test1'
            ,arrow: 'always'
        });

        //改变下时间间隔、动画类型、高度
        carousel.render({
            elem: '#test2'
            ,interval: 1800
            ,anim: 'fade'
            ,height: '120px'
        });

        //设定各种参数
        var ins3 = carousel.render({
            elem: '#test3'
        });
        //图片轮播
        carousel.render({
            elem: '#test10'
            ,width: '100%'
            ,height: '800px'
            ,interval: 3000
        });

        //事件
        carousel.on('change(test4)', function(res){
            console.log(res)
        });

        var $ = layui.$, active = {
            set: function(othis){
                var THIS = 'layui-bg-normal'
                    ,key = othis.data('key')
                    ,options = {};

                othis.css('background-color', '#5FB878').siblings().removeAttr('style');
                options[key] = othis.data('value');
                ins3.reload(options);
            }
        };

        //监听开关
        form.on('switch(autoplay)', function(){
            ins3.reload({
                autoplay: this.checked
            });
        });

        $('.demoSet').on('keyup', function(){
            var value = this.value
                ,options = {};
            if(!/^\d+$/.test(value)) return;

            options[this.name] = value;
            ins3.reload(options);
        });

        //其它示例
        $('.demoTest .layui-btn').on('click', function(){
            var othis = $(this), type = othis.data('type');
            active[type] ? active[type].call(this, othis) : '';
        });
    });
</script>
</body>
</html>
