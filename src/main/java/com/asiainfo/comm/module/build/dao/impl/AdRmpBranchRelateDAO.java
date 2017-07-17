package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdRmpBranchRelate;
import com.asiainfo.comm.module.models.query.QAdRmpBranchRelate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/3 0003.
 */
@Component
public class AdRmpBranchRelateDAO {

    public AdRmpBranchRelate qryByBranchId(Long branchId) {
        AdBranch adBranch = new AdBranch();
        adBranch.setBranchId(branchId);
        List<AdRmpBranchRelate> rmpBranchRelates = new QAdRmpBranchRelate().branch.branchId.eq(branchId).state.eq(1).findList();
        if (rmpBranchRelates != null && rmpBranchRelates.size() > 0) {
            return rmpBranchRelates.get(0);
        } else {
            return null;
        }
    }

}
