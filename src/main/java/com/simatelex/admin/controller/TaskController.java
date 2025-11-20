package com.simatelex.admin.controller;

import com.simatelex.admin.model.RespBean;
import com.simatelex.admin.pojo.TimingTask;
import com.simatelex.admin.query.TaskQuery;
import com.simatelex.admin.service.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author Nancy Zhao
 * @Date 2025/08/14 11:27
 * @Version 1.0
 **/
@Controller
@RequestMapping("task")
public class TaskController {

    @Resource
    private ITaskService taskService;

    @RequestMapping("index")
    public String home() {
        return "task/task";
    }

    @RequestMapping("list")
    @ResponseBody
    public Map<String, Object> taskList(TaskQuery taskQuery){
        return taskService.taskList(taskQuery);
    }

    @RequestMapping("addOrUpdatePage")
    public String addOrUpdatePage(Integer id, Model model) {
        model.addAttribute("task", id != null ? taskService.getById(id) : null);
        return "task/add_update";//返回页面
    }

    @RequestMapping("save")
    @ResponseBody
    public RespBean saveTask(TimingTask timingTask){
        taskService.saveTask(timingTask);
        return RespBean.success("数据添加成功！");
    }

    @RequestMapping("update")
    @ResponseBody
    public RespBean updateTask(TimingTask timingTask){
        taskService.updateTask(timingTask);
        return RespBean.success("数据添加成功！");
    }

    @RequestMapping("timingStartAll")
    @ResponseBody
    public RespBean timingStartAll(Integer[] ids) {
        taskService.timingStartAll(ids);
        return RespBean.success("任务全部开始成功！");
    }
}
