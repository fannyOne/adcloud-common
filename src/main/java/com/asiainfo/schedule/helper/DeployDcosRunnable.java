package com.asiainfo.schedule.helper;

import com.asiainfo.comm.common.pojo.pojoExt.NumberCount;
import com.asiainfo.comm.module.models.AdDcosDeployInfo;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import com.asiainfo.comm.module.deploy.service.impl.DcosApiImpl;

import java.util.HashSet;

/**
 * Created by YangRY
 * 2016/10/19 0019.
 */
public class DeployDcosRunnable implements Runnable {
    AdSystemDeployLog deployLog;
    AdDcosDeployInfo adDcosDeployInfo;
    String appIdStr;
    NumberCount runNum;
    DcosApiImpl dcosApiUtil;
    private StringBuilder sb;
    private HashSet<String> firstAppId;

    public DeployDcosRunnable(AdSystemDeployLog deployLog, AdDcosDeployInfo adDcosDeployInfo, String appIdStr, NumberCount runNum, DcosApiImpl dcosApiUtil, StringBuilder sb, HashSet<String> firstAppId) {
        this.deployLog = deployLog;
        this.adDcosDeployInfo = adDcosDeployInfo;
        this.appIdStr = appIdStr;
        this.runNum = runNum;
        this.dcosApiUtil = dcosApiUtil;
        this.sb = sb;
        this.firstAppId = firstAppId;
    }

    @Override
    public void run() {
        try {
            if (firstAppId.contains(appIdStr)) {
                dcosApiUtil.checkFirstDeploySync(deployLog, appIdStr, sb);
            } else {
                dcosApiUtil.deployDcos(deployLog, appIdStr, sb);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dcosApiUtil.logAppend(sb, "deploy", appIdStr, false, e.getMessage());
            dcosApiUtil.saveFailedDeployLog(deployLog, sb);
        } finally {
            runNum.subNumber();
        }
    }
}
