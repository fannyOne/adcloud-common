package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdBuildRmpLog;
import com.asiainfo.comm.module.models.query.QAdBuildRmpLog;
import org.springframework.stereotype.Component;

/**
 * Created by HK on 2016/8/15.
 */
@Component
public class AdBuildRmpLogDAO {
    public int qryRowByCommitIdAndBranchId(String commitId, long branchId) {
        return new QAdBuildRmpLog().state.eq(1).adBranch.branchId.eq(branchId).commitId
            .eq(commitId).findRowCount();
    }

    public void save(AdBuildRmpLog log) {
        log.save();
    }
}
