package com.example.task;

import com.example.entity.RuntimeDetail;
import com.example.utils.MonitorUtils;
import com.example.utils.NetUtils;
import jakarta.annotation.Resource;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.management.monitor.Monitor;

public class MonitorJobBean implements Job {
    @Resource
    MonitorUtils monitor;

    @Resource
    NetUtils net;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        RuntimeDetail detail = monitor.monitorRuntimeDetail();
//        System.out.println(detail);
        net.updateRuntimeDetails(detail);
    }
}
