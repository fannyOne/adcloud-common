package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdPipeLineStateDAO;
import com.asiainfo.comm.module.models.AdPipeLineState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */
@Component
public class AdPipeLineStateImpl {
    @Autowired
    AdPipeLineStateDAO pipeLineStateDao;

    public void updatePipeLineState(AdPipeLineState pipeLineState) {
        pipeLineStateDao.updatePipeLineState(pipeLineState);
    }

    public List<AdPipeLineState> qryByLastBuildResult(long branchId, int buildResult) {
        return pipeLineStateDao.qryByLastBuildResult(branchId, buildResult);
    }
}
