package com.asiainfo.comm.module.autoTest.dao.impl;

import com.asiainfo.comm.module.models.AdAutoTestLog;
import com.asiainfo.comm.module.models.query.QAdAutoTestLog;
import com.avaje.ebean.PagedList;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */
@Component
public class AdAutoTestLogDAO {

    public void save(AdAutoTestLog log) {
        log.save();
    }

    public PagedList<AdAutoTestLog> qryAutoTestLogByBranchId(long branchId, int testType, Integer page, Integer pageSize) {
        return new QAdAutoTestLog().adBranch.branchId.eq(branchId).state.eq(1).autoType.eq(testType)
            .findPagedList(page, pageSize);
    }

    public AdAutoTestLog qryAutoTestLogBySeqId(long seqId) {
        List<AdAutoTestLog> list = new QAdAutoTestLog().seqId.eq(seqId).state.eq(1).findList();
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

}
