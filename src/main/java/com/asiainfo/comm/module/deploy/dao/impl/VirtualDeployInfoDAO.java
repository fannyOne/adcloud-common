package com.asiainfo.comm.module.deploy.dao.impl;

import com.asiainfo.comm.module.models.AdVirtualEnvironment;
import com.asiainfo.comm.module.models.query.QAdVirtualEnvironment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by guojian on 9/28/16.
 */
@Component
public class VirtualDeployInfoDAO {
    public List<AdVirtualEnvironment> findByProjectId(Long projectId) {
        List<AdVirtualEnvironment> ave = new QAdVirtualEnvironment().adProject.projectId.eq(projectId).findList();
        return ave;
    }

    public AdVirtualEnvironment findById(Long id) {
        AdVirtualEnvironment ave = new QAdVirtualEnvironment().virtualId.eq(id).findUnique();
        return ave;
    }
}
