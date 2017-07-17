package com.asiainfo.schedule.helper;

import com.asiainfo.comm.common.pojo.pojoExt.NumberCount;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import com.asiainfo.comm.module.deploy.service.impl.DcosApiImpl;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhangpeng on 2017/5/19.
 */
public class StartDcosRunnable implements Runnable {
    private AdSystemDeployLog deployLog;
    private String appIdStr;
    private NumberCount runNum;
    private DcosApiImpl dcosApiUtil;
    private StringBuilder sb;

    public StartDcosRunnable(AdSystemDeployLog deployLog, String appIdStr, NumberCount runNum, DcosApiImpl dcosApiUtil, StringBuilder sb) {
        this.deployLog = deployLog;
        this.appIdStr = appIdStr;
        this.runNum = runNum;
        this.dcosApiUtil = dcosApiUtil;
        this.sb = sb;
    }


    @Override
    public void run() {
        try {
            dcosApiUtil.startDocsScale(deployLog, appIdStr, sb);
        } catch (Exception e) {
            e.printStackTrace();
            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sb.append("\n").append(sFormat.format(new Date())).append(": ").append(appIdStr).append(" failed: <h class=\"dcos-deploy-error-msg\">").append(e.getMessage()).append("</h>");
            dcosApiUtil.saveFailedDeployLog(deployLog, sb);
        } finally {
            runNum.subNumber();
        }
    }
}