package com.asiainfo.comm.module.deploy.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.AdEnvPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.AdEnvPojo;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosDeployInfoDAO;
import com.asiainfo.comm.module.models.AdDcosDeployInfo;
import com.avaje.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@lombok.extern.slf4j.Slf4j
public class DcosDeployInfoImpl {
    @Autowired
    AdDcosDeployInfoDAO adDcosDeployInfoDAO;
    @Value("${dcosApi.url}")
    String dcosApiUrl;


    public AdDcosDeployInfo qryDcosDeployInfoById(long deployInfoId) {
        return adDcosDeployInfoDAO.qryDcosDeployInfoById(deployInfoId);
    }

    public List<AdDcosDeployInfo> qryDcosDeployInfoByProjectId(long projectId) {
        return adDcosDeployInfoDAO.qryDcosDeployInfoByProjectId(projectId);
    }

    public AdEnvPojo qryEnvInfo(Long projectId, String branchType) {
        AdEnvPojo poj = new AdEnvPojo();
        List<SqlRow> sqlRowList = adDcosDeployInfoDAO.qryEnvInfo(projectId, branchType);
        List<AdEnvPojoExt> envList = new ArrayList<>();
        for (SqlRow sqlRow : sqlRowList) {
            AdEnvPojoExt ext = new AdEnvPojoExt();
            ext.setEnvId(sqlRow.getString("ENV_ID"));
            ext.setEnvName(sqlRow.getString("ENV_NAME"));
            envList.add(ext);
        }
        poj.setEnvList(envList);
        return poj;
    }

}
