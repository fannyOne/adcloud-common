package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdProjectGitTags;
import com.asiainfo.comm.module.models.query.QAdProjectGitTags;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/6/6.
 */
@Component
public class AdProjectGitTagsDAO {
    public AdProjectGitTags qryByProTagId(long proTagId) {
        AdProjectGitTags adProjectGitTags = new QAdProjectGitTags().proTagId.eq(proTagId).findUnique();
        return adProjectGitTags;

    }
}
