package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdJenkinsInfoDAO;
import com.asiainfo.comm.module.models.AdJenkinsInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by HK on 2016/8/9.
 */
@Component
public class AdJenkinsInfoImpl {
    @Autowired
    AdJenkinsInfoDAO jenkinsInfoDAO;


    public AdJenkinsInfo qryByJkId(Long jenkinsId) {
        return jenkinsInfoDAO.qryByJkId(jenkinsId);
    }


}
