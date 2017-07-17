package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdBranchShell;
import com.asiainfo.comm.module.models.query.QAdBranchShell;
import org.springframework.stereotype.Component;

/**
 * Created by yangry on 2016/6/16 0016.
 */
@Component
public class AdBranchRelatDAO {
    public AdBranchShell qryBranchShell(long branchId, long pipelineId) {
        if (branchId != 0) {
            return new QAdBranchShell().branchId.eq(branchId).pipelineId.eq(pipelineId).findUnique();
        }
        return null;
    }
}
