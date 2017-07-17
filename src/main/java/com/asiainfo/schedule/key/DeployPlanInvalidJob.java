package com.asiainfo.schedule.key;

import com.asiainfo.comm.module.build.service.impl.AdProjectDeployPackageImpl;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployLogImpl;
import com.asiainfo.comm.module.models.AdProjectDeployPackage;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import com.asiainfo.schedule.helper.DeployRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YangRY
 * 2016/10/21 0021.
 * 处理失效的发布计划
 */
@lombok.extern.slf4j.Slf4j
@Component
public class DeployPlanInvalidJob {
    @Autowired
    SystemDeployLogImpl systemDeployLogImpl;
    @Autowired
    AdProjectDeployPackageImpl projectDeployPackageImpl;

    @Async
    public void dealPlan() {
        log.error("DeployPlanInvalidJob is running");
        List<AdSystemDeployLog> logList = systemDeployLogImpl.qryInvalidPlan();
        for (AdSystemDeployLog log : logList) {
            AdProjectDeployPackage projectDeployPackage = projectDeployPackageImpl.qryById(log.getProTagId());
            if (projectDeployPackage == null) {
                continue;
            }
            Map<String, Object> params = new HashMap<>();
            params.put("logId", log.getLogId());
            params.put("projectId", log.getProjectId());
            params.put("envId", log.getEnvId());
            params.put("commitId", projectDeployPackage.getCommitId());
            params.put("envType", log.getHostType() == 2 ? "dcos" : "virtual");
            params.put("branchId", log.getBranchId());
            DeployRunnable runnable = new DeployRunnable(params);
            runnable.run();
        }
    }
}
