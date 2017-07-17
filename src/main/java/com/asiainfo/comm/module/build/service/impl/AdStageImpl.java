package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdStageDAO;
import com.asiainfo.comm.module.models.AdStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */
@Component
public class AdStageImpl {
    @Autowired
    AdStageDAO adStageDAO;

    @NotNull
    public AdStage qryByBranchAndStageCode(long branchId, int stageCode) {
        return adStageDAO.qryByBranchAndStageCode(branchId, stageCode);
    }

    public boolean updateState(AdStage adStage, int state) throws SQLException {
        return adStageDAO.updateState(adStage, state);
    }

    public AdStage qryById(long stagId) {
        return adStageDAO.qryById(stagId);
    }

    public List<AdStage> qryStageList(long branchId) {

        return adStageDAO.QryAdOperationByEnvIdType(branchId);
    }

    public String UpdateAdOperationByEnvId(String branchId, String state) {
        return adStageDAO.UpdateAdOperationByEnvId(branchId, state);
    }

    public void save(AdStage downloadStage) {
        adStageDAO.save(downloadStage);
    }

    public AdStage qryRunStage(Long branchId) {
        return adStageDAO.qryRunStage(branchId);
    }

    public List<AdStage> qryAfterStep(Long branchId, int step) {
        return adStageDAO.qryAfterStep(branchId, step);
    }

    public List<AdStage> qryAdStage(Long branchId) {
        return adStageDAO.qryAdStage(branchId);
    }

    public List<AdStage> qryAutoTestStages(int stageCode) {
        return adStageDAO.qryAutoTestStages(stageCode);
    }
}
