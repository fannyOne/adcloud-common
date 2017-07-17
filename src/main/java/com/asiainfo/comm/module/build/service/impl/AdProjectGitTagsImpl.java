package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdProjectGitTagsDAO;
import com.asiainfo.comm.module.models.AdProjectGitTags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by HK on 2016/8/9.
 */
@Component
public class AdProjectGitTagsImpl {
    @Autowired
    AdProjectGitTagsDAO adProjectGitTagsDAO;


    public AdProjectGitTags qryByProTagId(Long proTagId) {
        return adProjectGitTagsDAO.qryByProTagId(proTagId);
    }
}
