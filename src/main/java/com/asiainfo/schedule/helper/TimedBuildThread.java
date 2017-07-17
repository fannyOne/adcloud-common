package com.asiainfo.schedule.helper;

import com.asiainfo.comm.common.pojo.pojoMaster.ManualHandPojo;
import com.asiainfo.comm.module.build.service.impl.JenkinsImpl;
import com.asiainfo.comm.module.deploy.dao.impl.AdTimedBuildTaskLogDAO;

/**
 * Created by liusteven on 17/5/26.
 */
public class TimedBuildThread extends Thread {

    private JenkinsImpl jenkinsimpl;

    private AdTimedBuildTaskLogDAO adTimedBuildTaskLogDAO;

    private long branchId;

    private long userId;

    private int buildType;

    public TimedBuildThread(JenkinsImpl jenkinsimpl, AdTimedBuildTaskLogDAO adTimedBuildTaskLogDAO, long branchId, long userId, int buildType) {
        this.jenkinsimpl = jenkinsimpl;
        this.adTimedBuildTaskLogDAO = adTimedBuildTaskLogDAO;
        this.branchId = branchId;
        this.userId = userId;
        this.buildType = buildType;
    }

    @Override
    public void run() {
        ManualHandPojo manualHandPojo = jenkinsimpl.triggerJenkins(branchId, buildType, userId + "", false, "");
        try {
            if ("200".equals(manualHandPojo.getRetCode())) {
                adTimedBuildTaskLogDAO.save(branchId, "success", manualHandPojo.getRetMessage());
            } else {
                adTimedBuildTaskLogDAO.save(branchId, "fail", manualHandPojo.getRetMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
