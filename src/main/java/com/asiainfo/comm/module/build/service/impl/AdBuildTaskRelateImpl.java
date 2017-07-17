package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdBuildTaskRelateDAO;
import com.asiainfo.comm.module.models.AdBuildTaskRelate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */
@Component
public class AdBuildTaskRelateImpl {
    @Autowired
    AdBuildTaskRelateDAO buildTaskRelateDao;

    public void addRelate(String taskCodeBr, int isDev, long buildSeq, String codeListsStr) {
        buildTaskRelateDao.addRelate(taskCodeBr, isDev, buildSeq, codeListsStr);
    }

    public List<AdBuildTaskRelate> getSeqTaskByPubSeqId(long buildSeq) {
        return buildTaskRelateDao.getSeqTaskByPubSeqId(buildSeq);
    }
}
