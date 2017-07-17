package com.asiainfo.comm.module.deploy.dao.impl;

import com.asiainfo.comm.module.models.AdTimedBuildTaskLog;
import com.avaje.ebean.EbeanServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by liusteven on 17/6/2.
 */
@Repository
public class AdTimedBuildTaskLogDAO {

    @Qualifier("adcloud")
    @Autowired
    EbeanServer ebeanServer;


    public void beginTraction() {
        ebeanServer.beginTransaction();
    }

    public void endTraction() {
        ebeanServer.endTransaction();
    }

    public void commitTraction() {
        ebeanServer.commitTransaction();
    }

    public void save(Long branchId, String result, String detail) throws Exception {
        AdTimedBuildTaskLog adTimedBuildTaskLog = new AdTimedBuildTaskLog();
        adTimedBuildTaskLog.setBranchId(branchId);
        adTimedBuildTaskLog.setState(1);
        adTimedBuildTaskLog.setTime(new Date());
        adTimedBuildTaskLog.setResult(result);
        adTimedBuildTaskLog.setDetail(detail);
        save(adTimedBuildTaskLog);
    }

    public void save(AdTimedBuildTaskLog adTimedBuildTaskLog) throws Exception {
        if (adTimedBuildTaskLog != null) {
            ebeanServer.save(adTimedBuildTaskLog);
        }
    }
}
