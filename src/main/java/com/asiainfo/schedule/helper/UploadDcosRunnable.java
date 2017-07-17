package com.asiainfo.schedule.helper;

import com.asiainfo.comm.common.pojo.pojoExt.NumberCount;
import com.asiainfo.comm.module.models.AdDcosDeployInfo;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import com.asiainfo.comm.module.deploy.service.impl.DcosApiImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by yangry on 2016/12/23.
 */
public class UploadDcosRunnable implements Runnable {
    private HashSet<String> failedAppId;
    private String[] packages;
    private final String md5;
    private AdSystemDeployLog deployLog;
    private AdDcosDeployInfo adDcosDeployInfo;
    private String appIdStr;
    private NumberCount runNum;
    private DcosApiImpl dcosApiUtil;
    private StringBuilder sb;

    public UploadDcosRunnable(AdSystemDeployLog deployLog, AdDcosDeployInfo adDcosDeployInfo, HashSet<String> failedAppId, String[] packages, String md5, String appIdStr, NumberCount count, DcosApiImpl dcosApiUtil, StringBuilder sb) {
        this.deployLog = deployLog;
        this.adDcosDeployInfo = adDcosDeployInfo;
        this.failedAppId = failedAppId;
        this.packages = packages;
        this.md5 = md5;
        this.appIdStr = appIdStr;
        this.runNum = count;
        this.dcosApiUtil = dcosApiUtil;
        this.sb = sb;
    }

    @Override
    public void run() {
        try {
            dcosApiUtil.uploadPackageByAppId(deployLog, adDcosDeployInfo, failedAppId, packages, md5, appIdStr, sb);
        } catch (Exception e) {
            e.printStackTrace();
            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sb.append("\n<h class=\"dcos-deploy-time\">").append(sFormat.format(new Date())).append("</h> upload package ").append(packages[packages.length-1]).append(" for appId <h class=\"dcos-deploy-appid\">").append(appIdStr).append("</h> failed:<h class=\"dcos-deploy-error-msg\">").append(e.getMessage()).append("</h>");
            dcosApiUtil.saveFailedDeployLog(deployLog, sb);
        } finally {
            runNum.subNumber();
        }
    }
}
