package com.asiainfo.schedule.key;

import com.asiainfo.util.StringUtil;
import com.asiainfo.comm.module.build.service.impl.AdProjectDeployPackageImpl;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployLogImpl;
import com.asiainfo.comm.module.models.AdProjectDeployPackage;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import com.asiainfo.schedule.quartz.manager.QuartzManager;
import com.asiainfo.schedule.job.DeployManagerJob;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YangRY
 * 2016/10/21 0021.
 * 抢占发布计划
 */
@lombok.extern.slf4j.Slf4j
@Component
public class DeployPlanJob {
    @Autowired
    SystemDeployLogImpl systemDeployLogImpl;
    @Autowired
    AdProjectDeployPackageImpl projectDeployPackageImpl;

    @Async
    public void dealPlan() throws ParseException, SchedulerException {
        log.error("DeployPlanJob is running");
        List<AdSystemDeployLog> logList = systemDeployLogImpl.qryNoDealPlan();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (AdSystemDeployLog log : logList) {
            String jobToken = new StringUtil().getRandomStr(10);
            if (systemDeployLogImpl.changeState(log, 2, jobToken)) {
                AdProjectDeployPackage projectDeployPackage = projectDeployPackageImpl.qryById(log.getProTagId());
                String time = sdf.format(log.getRunTime());//计划时间
                Map<String, Object> params = new HashMap<>();
                params.put("branchId", log.getBranchId());
                params.put("logId", log.getLogId());
                params.put("projectId", log.getProjectId());
                params.put("envId", log.getEnvId());
                if (log.getOperType() != null && log.getOperType() == 1) {
                    params.put("commitId", projectDeployPackage.getCommitId());
                }
                params.put("operType", "" + log.getOperType());
                params.put("envType", log.getHostType() == 2 ? "dcos" : "virtual");
                DeployManagerJob job = new DeployManagerJob();
                //job生成一个token
                QuartzManager.addOneTimeJob(jobToken, job, time, params);
            }
        }
    }
}
