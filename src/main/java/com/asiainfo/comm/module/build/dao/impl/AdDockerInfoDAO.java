package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdDockerInfo;
import com.asiainfo.comm.module.models.query.QAdDockerInfo;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by zhangpeng on 2016/7/21.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class AdDockerInfoDAO {

    public List<AdDockerInfo> qryImagesStatus(String name, int status) {
        List<AdDockerInfo> list = new QAdDockerInfo().imagesName.eq(name).status.eq(status).findList();
        return list;
    }

    public List<AdDockerInfo> qryBranchStatus(String name, int branchId) {
        List<AdDockerInfo> list = new QAdDockerInfo().imagesName.eq(name).branchId.eq(branchId).findList();
        return list;
    }

    public List<AdDockerInfo> qryImagesName(String name) {
        List<AdDockerInfo> list = new QAdDockerInfo().imagesName.eq(name).findList();
        return list;
    }

    public void startContainersStatus(String name, String containers, int branch) {//TODO 往表里插入创建的容器信息
        AdDockerInfo adDockerInfo = new AdDockerInfo();
        adDockerInfo.setContainersId(containers);
        adDockerInfo.setBranchId(branch);
        adDockerInfo.setImagesName(name);
        adDockerInfo.setStatus(0);
        adDockerInfo.setCreateDate(new Date());
        adDockerInfo.save();
    }

    public void updateContainersStatus(String container) {
        AdDockerInfo adDockerInfo;
        List<AdDockerInfo> list = new QAdDockerInfo().containersId.eq(container).findList();
        adDockerInfo = list.get(0);
        adDockerInfo.setStatus(1);
        adDockerInfo.setUpdateDate(new Date());
        adDockerInfo.update();
    }
}
