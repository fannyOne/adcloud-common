package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdBuildReturnValueDAO;
import com.asiainfo.comm.module.models.AdBuildReturnValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by YangRY on 2016/8/9.
 */
@Component
public class AdBuildReturnValueImpl {
    @Autowired
    AdBuildReturnValueDAO returnValueDAO;

    public AdBuildReturnValue qryBuildReturnValue(Long pipelineId, long step) {
        return returnValueDAO.qryBuildReturnValue(pipelineId, step);
    }

    public void insertBuildReturnSeq(AdBuildReturnValue adBuildReturnValue) {
        returnValueDAO.insertBuildReturnSeq(adBuildReturnValue);
    }

    public void updateBuildReturnSeq(AdBuildReturnValue adBuildReturnValue) {
        returnValueDAO.updateBuildReturnSeq(adBuildReturnValue);
    }
}
