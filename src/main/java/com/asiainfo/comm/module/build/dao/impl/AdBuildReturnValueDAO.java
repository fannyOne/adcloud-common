package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdBuildReturnValue;
import com.asiainfo.comm.module.models.query.QAdBuildReturnValue;
import org.springframework.stereotype.Component;

/**
 * Created by weif on 2016/7/11.
 */
@Component
public class AdBuildReturnValueDAO {


    public void insertBuildReturnSeq(AdBuildReturnValue adBuildReturnValue) {
        if (adBuildReturnValue != null) {
            adBuildReturnValue.save();
        }
    }

    public void updateBuildReturnSeq(AdBuildReturnValue adBuildReturnValue) {
        if (adBuildReturnValue != null) {
            adBuildReturnValue.update();
        }
    }

    public AdBuildReturnValue qryBuildReturnValue(long pipelineId, long step) {
        AdBuildReturnValue adBuildReturnValue = new QAdBuildReturnValue().pipelineId.eq(pipelineId).step.eq(step).findUnique();
        return adBuildReturnValue;
    }


}
