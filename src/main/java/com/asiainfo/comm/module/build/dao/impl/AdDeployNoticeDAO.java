package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdDeployNotice;
import com.asiainfo.comm.module.models.query.QAdDeployNotice;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by weif on 2016/12/27.
 */
@Component
public class AdDeployNoticeDAO {

    public List<AdDeployNotice> qryAllDeployNotice() {
        List<AdDeployNotice> adDeployNoticeList = new QAdDeployNotice().createDate.desc().findList();
        return adDeployNoticeList;
    }

}
