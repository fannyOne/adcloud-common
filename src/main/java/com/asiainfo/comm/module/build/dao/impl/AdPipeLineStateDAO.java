package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdPipeLineState;
import com.asiainfo.comm.module.models.query.QAdPipeLineState;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by weif on 2016/6/15.
 */
@Component
public class AdPipeLineStateDAO {

    public void updatePipeLineState(AdPipeLineState adPipeLineState) {
        if (adPipeLineState != null) {
            String s = "UPDATE Ad_Pipeline_State set branch_state = :state,last_build_result=:dealresult where pipeline_id = :env_id";
            SqlUpdate update = Ebean.createSqlUpdate(s);
            update.setParameter("env_id", adPipeLineState.getPipelineId());
            update.setParameter("state", adPipeLineState.getBranchState());
            update.setParameter("dealresult", adPipeLineState.getLastBuildResult());
            int modifiedCount = Ebean.execute(update);
        }
    }

    public AdPipeLineState qryEnvById(long envId) {
        List<AdPipeLineState> states = new QAdPipeLineState().state.eq(1).adBranch.branchId.eq(envId).findList();
        if (states != null && states.size() > 0) {
            return states.get(0);
        }
        return null;
    }

    public AdPipeLineState qryByEnvIdBuildType(long envId, int buildType) {
        List<AdPipeLineState> states = new QAdPipeLineState().adBranch.branchId.eq(envId).buildType.eq(buildType).findList();
        if (states != null && states.size() > 0) {
            return states.get(0);
        }
        return null;
    }

    public List<AdPipeLineState> qryAllPipeLine() {
        List<AdPipeLineState> adPipeLineStateListd = new QAdPipeLineState().fetch("adBranch").adBranch.state.eq(1).findList();
        return adPipeLineStateListd;
    }

    public void savePipeLineState(AdPipeLineState adPipeLineState) {
        if (adPipeLineState != null) {
            Ebean.save(adPipeLineState);
        }
    }

    public AdPipeLineState qryById(long pipelineId) {
        List<AdPipeLineState> states = new QAdPipeLineState().state.eq(1).pipelineId.eq(pipelineId).findList();
        if (states != null && states.size() > 0) {
            return states.get(0);
        }
        return null;
    }

    public List<AdPipeLineState> qryByBranch(long branchId) {
        return new QAdPipeLineState().state.eq(1).adBranch.branchId.eq(branchId).findList();
    }

    public List<AdPipeLineState> qryByLastBuildResult(long branchId, int buildResult) {
        return new QAdPipeLineState().state.eq(1).lastBuildResult.eq(buildResult).adBranch.branchId.eq(branchId).findList();
    }
}
