package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdFastenSign;
import com.asiainfo.comm.module.models.query.QAdFastenSign;
import com.avaje.ebean.Ebean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by YangRY
 * 2016/10/27 0027.
 */
@Component
public class AdFastenSignDAO {
    public void save(AdFastenSign fastenSign) {
        fastenSign.save();
    }


    public long saveAndReturn(AdFastenSign fastenSign) {
        fastenSign.save();
        return fastenSign.getSignId();
    }

    public void save(List<AdFastenSign> fastenSignList) {
        Ebean.saveAll(fastenSignList);
    }

    public AdFastenSign qryByIdNoState(Long signId) {
        return new QAdFastenSign().signId.eq(signId).findUnique();
    }

    public AdFastenSign qryById(Long signId) {
        return new QAdFastenSign().signId.eq(signId).state.eq(1).findUnique();
    }

    public List<AdFastenSign> qryByUser(Long userId) {
        return new QAdFastenSign().adUser.userId.eq(userId).state.eq(1).orderBy(" CREATE_DATE DESC").findList();
    }

    public Integer qryCountByUserName(Long userId, String tagName) {
        return new QAdFastenSign().adUser.userId.eq(userId).signName.eq(tagName).state.eq(1).findRowCount();
    }

    public void updateAll(List<AdFastenSign> AdFastenSign) {
        if (!CollectionUtils.isEmpty(AdFastenSign)) {
            Ebean.updateAll(AdFastenSign);
        }
    }

    public List<AdFastenSign> qryByBranchId(Long branchId) {
        return new QAdFastenSign().adBranch.branchId.eq(branchId).state.eq(1).findList();
    }

    public List<AdFastenSign> qryByProjectId(Long projectId) {
        return new QAdFastenSign().adProject.projectId.eq(projectId).state.eq(1).findList();
    }

    public List<AdFastenSign> qryByGroupId(Long groupId) {
        return new QAdFastenSign().adGroup.groupId.eq(groupId).state.eq(1).findList();
    }

    public void deleteByBranchId(Long branchId) {
        List<AdFastenSign> adFastenSigns = qryByBranchId(branchId);
        setState(adFastenSigns, 0);
    }

    public void deleteByProjectId(Long projectId) {
        List<AdFastenSign> adFastenSigns = qryByProjectId(projectId);
        setState(adFastenSigns, 0);
    }

    public void deleteByGroupId(Long groupId) {
        List<AdFastenSign> adFastenSigns = qryByGroupId(groupId);
        setState(adFastenSigns, 0);
    }

    public void setState(List<AdFastenSign> adFastenSigns, Integer state) {
        if (null != adFastenSigns) {
            for (AdFastenSign fastenSign : adFastenSigns) {
                fastenSign.setState(state);
                fastenSign.setModifyDate(new Date());
            }
            updateAll(adFastenSigns);
        }
    }
}
