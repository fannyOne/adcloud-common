package com.asiainfo.schedule.job;

import com.SpringApplicationcontextUtil;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployLogImpl;
import com.asiainfo.comm.module.deploy.service.impl.VirtualDeployInfoImpl;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import com.asiainfo.comm.module.deploy.service.impl.DcosApiImpl;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Map;

/**
 * Created by GUOJIAN on 8/3/16.
 */
public class DeployManagerJob implements org.quartz.Job {

    public static void checkEnvTypeAndRun(int operType, Map<String, Object> params, AdSystemDeployLog deployLog, String commitId, Long projectId, Long envId, Long branchId) {
        if (params.get("envType").equals("dcos")) {//dcos环境发布
            DcosApiImpl util = (DcosApiImpl)SpringApplicationcontextUtil.getBean("DcosApiImpl");
            switch (operType) {
                case 1:  // 发布
                    util.deploy2docs(envId, projectId, commitId, deployLog, branchId);
                    break;
                case 2: case 4:  // 重启 或者 停止
                    StringBuilder sb = new StringBuilder(deployLog.getDeployComment() == null ? "" : deployLog.getDeployComment());
                    util.restartOrStopDocs(envId, deployLog, sb);
                    break;
                case 3: // 启动
                    util.startDcos(envId, deployLog);
                    break;
                default:
                    break;
            }

        } else {
            VirtualDeployInfoImpl virtualDeployInfoImpl = (VirtualDeployInfoImpl) SpringApplicationcontextUtil.getBean("virtualDeployInfoImpl");
            if (operType == 1)
                virtualDeployInfoImpl.deployVirturl(commitId, envId, deployLog, branchId);
            else
                virtualDeployInfoImpl.restartVirturl(envId, deployLog);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int operType = 1;
        Map<String, Object> params = context.getJobDetail().getJobDataMap();
        SystemDeployLogImpl systemDeployLogImpl = (SystemDeployLogImpl) SpringApplicationcontextUtil.getBean("SystemDeployLogImpl");
        Long logId = (Long) params.get("logId");
        AdSystemDeployLog deployLog = null;
        if (logId != null) {
            deployLog = systemDeployLogImpl.qryById(logId);
        }
        String jobName = context.getJobDetail().getKey().getName();
        if (deployLog == null || deployLog.getDeployResult() != 3 || deployLog.getPlanState() != 2 || !deployLog.getJobToken().equals(jobName)) {
            return;
        }
        deployLog.setDeployResult(4);
        systemDeployLogImpl.addLogsBySystemId(deployLog);
        String commitId = (String) params.get("commitId");//包版本对应的commitId
        Long projectId = (Long) params.get("projectId");//项目ID
        Long envId = (Long) params.get("envId");//环境ID
        Long branchId = (Long) params.get("branchId");
        if (params.get("operType") != null && StringUtils.isNotEmpty((String) params.get("operType"))) {
            operType = Integer.parseInt((String) params.get("operType"));
        }
        deployLog.setOperType(operType);
        checkEnvTypeAndRun(operType, params, deployLog, commitId, projectId, envId, branchId);
    }

}
