package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdDeployNoticeDAO;
import com.asiainfo.comm.module.models.AdDeployNotice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by weif on 2016/12/27.
 */
@Component
public class AdDeployNoticeImpl {

    @Autowired
    AdDeployNoticeDAO adDeployNoticeDAO;

    public List<AdDeployNotice> qryAllDeployNotice() {
        return adDeployNoticeDAO.qryAllDeployNotice();
    }
}
