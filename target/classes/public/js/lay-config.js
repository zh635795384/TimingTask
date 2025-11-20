/**
 * date:2022/08/30
 * author:nancy zhao
 * description:此处放layui自定义扩展
 */

window.rootPath = (function (src) {
    src = document.scripts[document.scripts.length - 1].src;
    return src.substring(0, src.lastIndexOf("/") + 1);
})();

layui.config({
    base: rootPath + "lay-module/",
    version: true
}).extend({
    layuimini: "layuimini/layuimini", // layuimini扩展
    jquery_cookie:'jquery-cookie/jquery.cookie'
});
