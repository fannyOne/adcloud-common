package com.asiainfo.comm.module.autoTest.service.impl;

import com.asiainfo.comm.module.autoTest.dao.impl.AdAutoTestLogDAO;
import com.asiainfo.comm.module.models.AdAutoTestLog;
import com.avaje.ebean.PagedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */
@Component
public class AdAutoTestLogImpl {
    @Autowired
    AdAutoTestLogDAO autoTestLogDao;


    public void save(AdAutoTestLog autoTestLog) {
        autoTestLogDao.save(autoTestLog);
    }

    public PagedList<AdAutoTestLog> qryAutoTestLogByBranchId(long branchId, Integer page, Integer pageSize, int testType) {
        return autoTestLogDao.qryAutoTestLogByBranchId(branchId, testType, page, pageSize);
    }
}
