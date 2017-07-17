package com.asiainfo.comm.module.deploy.dao.impl;


import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdDcosBranchRelate;
import com.asiainfo.comm.module.models.AdDcosDeployInfo;
import com.asiainfo.comm.module.models.query.QAdDcosBranchRelate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by weif on 2017/3/20.
 */
@Component
public class AdDcosBranchRelateDAO {

    public List<AdDcosBranchRelate> qryBranchsByEnvId(Long envId) {
        List<AdDcosBranchRelate> adDcosBranchRelateList = new QAdDcosBranchRelate().state.eq(1).adDcosDeployInfo.deployInfoId.eq(envId).findList();
        return adDcosBranchRelateList;
    }

    /**
     * @param adBranch
     * @param adDcosDeployInfo
     * @param state
     * @param date
     */
    public void addDcosBranchRelate(AdBranch adBranch, AdDcosDeployInfo adDcosDeployInfo, Long state, Date date) {
        AdDcosBranchRelate adDcosBranchRelate = new AdDcosBranchRelate();
        adDcosBranchRelate.setAdBranch(adBranch);
        adDcosBranchRelate.setAdDcosDeployInfo(adDcosDeployInfo);
        adDcosBranchRelate.setState(state);
        adDcosBranchRelate.setCreateDate(date);
        adDcosBranchRelate.save();
    }

    /**
     * @param envId
     */
    public void deleteRelationByEnvId(Long envId) {
        List<AdDcosBranchRelate> adDcosBranchRelateList = new QAdDcosBranchRelate().adDcosDeployInfo.deployInfoId.eq(envId).findList();
        if (!CollectionUtils.isEmpty(adDcosBranchRelateList)) {
            for (AdDcosBranchRelate adDcosBranchRelate : adDcosBranchRelateList) {
                adDcosBranchRelate.setState(0L);
                adDcosBranchRelate.save();
            }
        }
    }

    public void deleteRelationByBranchId(Long branchId) {
        List<AdDcosBranchRelate> adDcosBranchRelateList = new QAdDcosBranchRelate().state.eq(1).adBranch.branchId.eq(branchId).findList();
        if (!CollectionUtils.isEmpty(adDcosBranchRelateList)) {
            for (AdDcosBranchRelate adDcosBranchRelate : adDcosBranchRelateList) {
                adDcosBranchRelate.setState(0L);
                adDcosBranchRelate.save();
            }
        }
    }

}
