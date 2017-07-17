package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdDcosDeployDtlDAO;
import com.asiainfo.comm.module.models.AdDcosDeployDtl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by zhangpeng on 2016/10/27.
 */
@Component
public class AdDcosDeployDtlImpl {
    @Autowired
    AdDcosDeployDtlDAO adDcosDeployDtlDAO;

    public String getDcosAppIdByDeployInfoId(long deployInfoId) {
        StringBuffer sbf = new StringBuffer();
        String showAppId ;
        String appId ;
        List<AdDcosDeployDtl> adDcosDeployDtlList = adDcosDeployDtlDAO.qryBranchsByDeployInfoId(deployInfoId);
        if (CollectionUtils.isEmpty(adDcosDeployDtlList))
            return null;
        for (AdDcosDeployDtl it : adDcosDeployDtlList) {
            sbf.append(it.getAppid() + ",");
        }
        String allAppId = sbf.toString();
        allAppId = allAppId.substring(0, allAppId.length() - 1);
        if (allAppId.length() > 15) {
            showAppId = allAppId.substring(0, 15) + "...";
        } else {
            showAppId = allAppId;
        }
        appId = "<span title=\"" + allAppId +"\">" + showAppId + "</span>";
        return appId;
    }
    /************************************改造方法****************************************************/
    /**
     * 根据deployInfoId删除dtl信息
     *
     * @param deployInfoId
     * @param state
     */
    public void deleteStateByDeployInfoId(long deployInfoId, int state) {
        adDcosDeployDtlDAO.deleteStateByDeployInfoId(deployInfoId, state);
    }

    /**
     * 修改dtl中数据状态
     *
     * @param deployInfoId 环境id
     * @param state        状态
     */
    public void updateStateByDeployInfoId(long deployInfoId, int state) {
        adDcosDeployDtlDAO.updateStateByDeployInfoId(deployInfoId, state);
    }

    /**
     * @param dcosInfoId 根据dcosInfoId 查询dtl
     * @return
     */
    public List<AdDcosDeployDtl> qryDcosDeployDtlByDcosInfoId(long dcosInfoId) {
        return adDcosDeployDtlDAO.qryDcosDeployDtlByDcosInfoId(dcosInfoId);
    }
}
