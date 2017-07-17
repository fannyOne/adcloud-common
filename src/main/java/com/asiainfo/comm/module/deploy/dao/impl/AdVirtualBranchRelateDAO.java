package com.asiainfo.comm.module.deploy.dao.impl;

import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdVirtualBranchRelate;
import com.asiainfo.comm.module.models.AdVirtualEnvironment;
import com.asiainfo.comm.module.models.query.QAdVirtualBranchRelate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by dlyxn on 2017/3/15.
 */
@Component
public class AdVirtualBranchRelateDAO {

    /**
     * 根据传递的信息，保存VirtualBranchRelate
     *
     * @param adBranch             分支信息
     * @param adVirtualEnvironment 环境信息
     * @param state                状态
     * @param date                 日期
     */
    public void addVirtualBranchRelate(AdBranch adBranch, AdVirtualEnvironment adVirtualEnvironment, Long state, Date date) {
        AdVirtualBranchRelate adVirtualBranchRelate = new AdVirtualBranchRelate();      //设置信息
        adVirtualBranchRelate.setAdBranch(adBranch);
        adVirtualBranchRelate.setAdVirtualEnvironment(adVirtualEnvironment);
        adVirtualBranchRelate.setState(state);
        adVirtualBranchRelate.setCreateDate(date);
        adVirtualBranchRelate.save();                                                   //设置完毕，存储信息
    }


    public List<AdVirtualBranchRelate> qryBranchsByEnvId(Long envId) {
        List<AdVirtualBranchRelate> adVirtualBranchRelateList = new QAdVirtualBranchRelate().state.eq(1).adVirtualEnvironment.virtualId.eq(envId).findList();
        return adVirtualBranchRelateList;
    }

    public void deleteRelationByBranchId(Long branchId) {
        List<AdVirtualBranchRelate> adVirtualBranchRelates = new QAdVirtualBranchRelate().state.eq(1).adBranch.branchId.eq(branchId).findList();
        if (adVirtualBranchRelates != null && adVirtualBranchRelates.size() > 0) {
            for (AdVirtualBranchRelate adVirtualBranchRelate : adVirtualBranchRelates) {
                adVirtualBranchRelate.setState(0L);
                adVirtualBranchRelate.save();
            }
        }

    }

    public void deleteRelationByEnvId(Long envId) {
        List<AdVirtualBranchRelate> adVirtualBranchRelates = new QAdVirtualBranchRelate().adVirtualEnvironment.virtualId.eq(envId).findList();
        if (adVirtualBranchRelates != null && adVirtualBranchRelates.size() > 0) {
            for (AdVirtualBranchRelate adVirtualBranchRelate : adVirtualBranchRelates) {
                adVirtualBranchRelate.setState(0L);
                adVirtualBranchRelate.save();
            }
        }

    }

}
