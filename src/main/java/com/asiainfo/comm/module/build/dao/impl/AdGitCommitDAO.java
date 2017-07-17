package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdProjectGitTags;
import com.asiainfo.comm.module.models.query.QAdProjectGitTags;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by yangry on 2016/6/16 0016.
 */
@Component
public class AdGitCommitDAO {

    public List<AdProjectGitTags> qryProjectVersion(long projectId) throws Exception {
        QAdProjectGitTags qAdProjectGitTags = new QAdProjectGitTags();
        List<AdProjectGitTags> projectGitTagsList = qAdProjectGitTags.projectId.eq(projectId).orderBy().proTagId.desc().findList();
        return projectGitTagsList;
    }

    public List<AdProjectGitTags> qryProjectVersionByBranch(long branchId) {
        QAdProjectGitTags qAdProjectGitTags = new QAdProjectGitTags();
        List<AdProjectGitTags> projectGitTagsList = qAdProjectGitTags.adBranch.branchId.eq(branchId).orderBy().proTagId.desc().findList();
        return projectGitTagsList;
    }

    public List<AdProjectGitTags> qryProjectVersion(long projectId, Long branchId) {
        QAdProjectGitTags qAdProjectGitTags = new QAdProjectGitTags();
        qAdProjectGitTags.adBranch.branchId.eq(branchId);
        List<AdProjectGitTags> projectGitTagsList = qAdProjectGitTags.projectId.eq(projectId).orderBy().proTagId.desc().findList();
        return projectGitTagsList;
    }
}
