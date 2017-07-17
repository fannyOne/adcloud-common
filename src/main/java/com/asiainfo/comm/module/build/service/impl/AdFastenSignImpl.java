package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoMaster.FlagPojo;
import com.asiainfo.comm.module.build.dao.impl.AdFastenSignDAO;
import com.asiainfo.comm.module.models.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by YangRY
 * 2016/10/27 0027.
 */
@Component
public class AdFastenSignImpl {
    @Autowired
    AdFastenSignDAO fastenSignDAO;

    public void save(AdFastenSign fastenSign) {
        fastenSignDAO.save(fastenSign);
    }

    public void save(List<AdFastenSign> fastenSignList) {
        fastenSignDAO.save(fastenSignList);
    }

    public AdFastenSign qryById(Long signId) {
        return fastenSignDAO.qryById(signId);
    }

    public AdFastenSign qryByIdNoState(Long signId) {
        return fastenSignDAO.qryByIdNoState(signId);
    }

    public List<AdFastenSign> qryByUser(Long userId) {
        return fastenSignDAO.qryByUser(userId);
    }

    public Integer qryCountByUserName(Long userId, String tagName) {
        return fastenSignDAO.qryCountByUserName(userId, tagName);
    }

    public FlagPojo fastenSign(Long userId, String tagName, String param, Integer pageType) {
        Integer count = qryCountByUserName(userId, tagName);
        FlagPojo pojo = new FlagPojo();
        if (count <= 0) {
            AdUser user = new AdUser();
            user.setUserId(userId);
            AdFastenSign fastenSign = new AdFastenSign();
            fastenSign.setAdUser(user);
            fastenSign.setSignName(tagName);
            fastenSign.setSignParam(param);
            JSONObject obj = JSONObject.fromObject(param);
            if (obj.has("branchId")) {
                AdBranch adBranch = new AdBranch();
                adBranch.setBranchId(Long.valueOf(obj.get("branchId").toString()));
                fastenSign.setAdBranch(adBranch);
            }
            if (obj.has("projectId")) {
                AdProject adProject = new AdProject();
                adProject.setProjectId(Long.valueOf(obj.get("projectId").toString()));
                fastenSign.setAdProject(adProject);
            }
            if (obj.has("group")) {
                AdGroup adGroup = new AdGroup();
                JSONObject.fromObject(obj.get("group")).get("groupId");
                adGroup.setGroupId(Long.valueOf(JSONObject.fromObject(obj.get("group")).get("groupId").toString()));
                fastenSign.setAdGroup(adGroup);
            }
            fastenSign.setSignType(pageType);
            fastenSign.setState(1);
            fastenSign.setCreateDate(new Date());
            long signId = fastenSignDAO.saveAndReturn(fastenSign);
            pojo.setRetMessage(signId + "");
        } else {
            pojo.setFlag("false");
            pojo.setRetMessage("请勿重复鎖定标签");
        }
        return pojo;
    }

    public void delete(AdFastenSign fastenSign) {
        fastenSign.setModifyDate(new Date());
        fastenSign.setState(0);
        fastenSignDAO.save(fastenSign);
    }

    public void deleteByGroupId(Long groupId) {
        fastenSignDAO.deleteByGroupId(groupId);
    }

    public void deleteByBranchId(Long branchId) {
        fastenSignDAO.deleteByBranchId(branchId);
    }
}
