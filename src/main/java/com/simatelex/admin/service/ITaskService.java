package com.simatelex.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simatelex.admin.pojo.TimingTask;
import com.simatelex.admin.query.TaskQuery;

import java.util.Map;

/**
 * @Author Nancy Zhao
 * @Date 2022/10/27 12:11
 * @Version 1.0
 **/
public interface ITaskService extends IService<TimingTask> {

    Map<String, Object> taskList(TaskQuery taskQuery);

    void saveTask(TimingTask timingTask);

    void updateTask(TimingTask timingTask);

    void timingStartAll(Integer[] ids);
}
