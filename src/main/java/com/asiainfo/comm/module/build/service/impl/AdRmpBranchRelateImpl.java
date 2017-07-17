package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdRmpBranchRelateDAO;
import com.asiainfo.comm.module.models.AdRmpBranchRelate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by YangRY
 * 2016/7/3 0003.
 */
@Component
public class AdRmpBranchRelateImpl {
    @Autowired
    AdRmpBranchRelateDAO relateDao;

    public AdRmpBranchRelate qryByBranchId(Long branchId) {
        return relateDao.qryByBranchId(branchId);
    }
}
