package com.asiainfo.schedule.helper;

import com.SpringApplicationcontextUtil;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployLogImpl;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.asiainfo.schedule.job.DeployManagerJob.checkEnvTypeAndRun;

/**
 * Created by YangRY
 * 2016/10/19 0019.
 */
public class DeployRunnable implements Runnable {
    private Map<String, Object> params;

    public DeployRunnable(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public void run() {
        int operType = 1;
        SystemDeployLogImpl systemDeployLogImpl = (SystemDeployLogImpl) SpringApplicationcontextUtil.getBean("SystemDeployLogImpl");
        Long logId = (Long) params.get("logId");
        AdSystemDeployLog deployLog;
        if (logId != null) {
            deployLog = systemDeployLogImpl.qryById(logId);
            deployLog.setDeployResult(4);
        } else {
            return;
        }
        systemDeployLogImpl.addLogsBySystemId(deployLog);
        String commitId = (String) params.get("commitId");//包版本对应的commitId
        Long projectId = (Long) params.get("projectId");//项目ID
        Long envId = (Long) params.get("envId");//环境ID
        Long branchId = Long.parseLong((String) params.get("branchId"));
        if (params.get("operType") != null && StringUtils.isNotEmpty((String) params.get("operType"))) {
            operType = Integer.parseInt((String) params.get("operType"));
        }
        deployLog.setOperType(operType);
        checkEnvTypeAndRun(operType, params, deployLog, commitId, projectId, envId, branchId);
    }
}
