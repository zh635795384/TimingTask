package com.simatelex.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simatelex.admin.mappers.xfstd01.TaskMapper;
import com.simatelex.admin.pojo.TimingTask;
import com.simatelex.admin.query.TaskQuery;
import com.simatelex.admin.service.ITaskService;
import com.simatelex.admin.util.AssertUtil;
import com.simatelex.admin.util.GetPCName;
import com.simatelex.admin.util.PageResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author Nancy Zhao
 * @Date 2023/06/28 11:06
 * @Version 1.0
 **/
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, TimingTask> implements ITaskService {

    @Override
    public Map<String, Object> taskList(TaskQuery taskQuery) {
        //分页，两个参数，一个当前页，一个条数
        IPage<TimingTask> page = new Page<>(taskQuery.getPage(), taskQuery.getLimit());
        //开启多条件查询
        QueryWrapper<TimingTask> queryWrapper = new QueryWrapper<>();
        //将查询结果返回给Page
        page = this.baseMapper.selectPage(page, queryWrapper);
        return PageResultUtil.getResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void saveTask(TimingTask timingTask) {
        publicCheck(timingTask);
        AssertUtil.isTrue(this.baseMapper.insert(timingTask) == 0, "数据更新失败！");
    }

    @Override
    public void updateTask(TimingTask timingTask) {
        publicCheck(timingTask);
        AssertUtil.isTrue(this.baseMapper.updateById(timingTask) == 0, "数据更新失败！");
    }

    private void publicCheck(TimingTask timingTask) {
        AssertUtil.isTrue(StringUtils.isBlank(timingTask.getFilePath()), "请输入完整文件路径！");
        AssertUtil.isTrue(StringUtils.isBlank(timingTask.getPeriod()), "请选择任务周期！");
        AssertUtil.isTrue(timingTask.getTaskTime() <= 0, "请输入间隔时间！");
        AssertUtil.isTrue(StringUtils.isBlank(timingTask.getTimeUnit()), "请输入时间单位！");
        timingTask.setPcName(GetPCName.getComputerName());
        timingTask.setCreateUser(GetPCName.getComputerName());
    }

    @Override
    public void timingStartAll(Integer[] ids) {
        for (Integer id : ids) {
            TimingTask timingTask = this.baseMapper.selectById(id);
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            if (timingTask.getIsDel().equals(true)) {
                this.timingStart(timingTask,scheduler);
            } else {
                this.timingStop(scheduler);
            }
        }
    }

    //    @Override
    public void timingStart(TimingTask timingTask,ScheduledExecutorService scheduler) {
        TimeUnit timeUnit = null;
        if (timingTask.getTimeUnit().equals("second")) {
            timeUnit = TimeUnit.SECONDS;
        } else if (timingTask.getTimeUnit().equals("minute")) {
            timeUnit = TimeUnit.MINUTES;
        } else if (timingTask.getTimeUnit().equals("hour")) {
            timeUnit = TimeUnit.HOURS;
        } else if (timingTask.getTimeUnit().equals("day")) {
            timeUnit = TimeUnit.DAYS;
        }
        scheduler.scheduleAtFixedRate(() -> {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", timingTask.getFilePath());
            int lastIndex = timingTask.getFilePath().lastIndexOf("\\");
            String newFilePath = timingTask.getFilePath().substring(0,lastIndex+1);
            System.out.println(newFilePath);
//            AssertUtil.isTrue(true, "test");
            processBuilder.directory(new File(newFilePath));
            processBuilder.redirectErrorStream(true); //继承当前进程的I/O流
            try {
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                log.trace("程序结束，退出代码：" + exitCode);
                if (exitCode != 0) {
                    log.trace("程序因错误退出，即将重启...");
//                timmingRestart(locale);
                } else {
                    log.trace("程序正常结束，不会重启");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();    //打印异常
            }
        }, 0, timingTask.getTaskTime(), timeUnit);  //initialDelay是首次的延迟时间，不需要延迟就是0
        /*try {
            TimingRestartUtils timingRestartUtils = new TimingRestartUtils("D:\\Program\\AutoEmail\\execAUTODL\\RestartAutoDL.bat", "D:\\Program\\AutoEmail\\execAUTODL\\", 5, TimeUnit.SECONDS);
            timingRestartUtils.start();
            // 添加关闭钩子
            *//*Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                systemLogService.addToLog("/mail/timingRestart", "用户" + UserUtils.getCurrentAdmin().getUsername() + "开始运行！程序关闭，停止定时任务...", "");
                timingRestartUtils.stop();
            }));*//*
        } catch (Exception e) {
            systemLogService.addToLog("/mail/timingRestart", "用户" + UserUtils.getCurrentAdmin().getUsername() + "开始运行！初始化失败: " + e.getMessage(), "");
            e.printStackTrace();
        }*/
    }

    public void timingStop(ScheduledExecutorService scheduler) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            log.trace("程序终止");
        }));
    }
}
