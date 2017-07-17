package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdDcosDeployDtl;
import com.asiainfo.comm.module.models.query.QAdDcosDeployDtl;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangpeng on 2016/10/27.
 */
@Component
public class AdDcosDeployDtlDAO {

    public void updateStateByBranchId(long branchId, int state) {
        String sql = "update ad_dcos_deploy_dtl t set t.state= :state  where t.branch_id= :branchId and t.state=1";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("branchId", branchId);
        update.setParameter("state", state);
        Ebean.execute(update);
    }

    public List<AdDcosDeployDtl> qryByBranchAndAppIds(Long branchId, String appId) {
        String[] appIds = StringUtils.split(appId, ",");
        List<String> ids = new ArrayList<>();
        for (String type : appIds) {
            ids.add("'" + type.replace(",", "','") + "'");
        }
        String sql = " WHERE BRANCH_ID = :branchId AND APPID IN (:appId) AND STATE = 1 ORDER BY PRIORITY_NUM ASC";
        return Ebean.createQuery(AdDcosDeployDtl.class, sql).setParameter("appId", ids).setParameter("branchId", branchId).findList();
    }
    /************************************************************改造方法******************************************/
    /**
     * 根据deployInfoId和状态删除信息
     *
     * @param deployInfoId dcos环境id
     * @param state        状态
     */
    public void deleteStateByDeployInfoId(long deployInfoId, int state) {
        String sql = "delete ad_dcos_deploy_dtl t where t.deploy_info_id= :deployInfoId and t.state= :state";//要执行的sql语句
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("deployInfoId", deployInfoId);
        update.setParameter("state", state);
        Ebean.execute(update);
    }

    /**
     * @param deployInfoId
     * @param state
     */
    public void updateStateByDeployInfoId(long deployInfoId, int state) {
        String sql = "update ad_dcos_deploy_dtl t set t.state= :state  where t.deploy_info_id= :deployInfoId and t.state=1";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("deployInfoId", deployInfoId);
        update.setParameter("state", state);
        Ebean.execute(update);
    }


    public List<AdDcosDeployDtl> qryDcosDeployDtlByDcosInfoId(long dcosInfoId) {
        return new QAdDcosDeployDtl().deployInfoId.eq(dcosInfoId).state.eq(1).orderBy("priorityNum").findList();
    }

    public List<AdDcosDeployDtl> qryBranchsByDeployInfoId(Long deployInfoId) {                                                  //王昊一改为通过DeployInfoID来查找部署IP
        return new QAdDcosDeployDtl().state.eq(1).deployInfoId.eq(deployInfoId).findList();

    }

    public List<AdDcosDeployDtl> qryByDcosDeployDtlAndAppIds(long dcosInfoId, String appId) {
        String[] appIds = StringUtils.split(appId, ",");
        List<String> ids = new ArrayList<>();
        for (String type : appIds) {
            ids.add(type);
        }
        String sql = " WHERE DEPLOY_INFO_ID = :dcosInfoId AND APPID IN (:appId) AND STATE = 1 ORDER BY PRIORITY_NUM ASC";
        return Ebean.createQuery(AdDcosDeployDtl.class, sql).setParameter("dcosInfoId", dcosInfoId).setParameter("appId", ids).findList();
    }

    public void updateAll(List<AdDcosDeployDtl> adDcosDeployDtls) {
        if (!CollectionUtils.isEmpty(adDcosDeployDtls)) {
            Ebean.updateAll(adDcosDeployDtls);
        }
    }
}
