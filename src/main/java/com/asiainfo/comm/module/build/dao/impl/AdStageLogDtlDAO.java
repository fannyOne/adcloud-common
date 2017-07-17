package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdStageLogDtl;
import com.asiainfo.comm.module.models.query.QAdStageLogDtl;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by weif on 2016/6/15.
 */
@Component
public class AdStageLogDtlDAO {

    public void insertStageLogDtl(AdStageLogDtl buildLogDtl) {
        if (buildLogDtl != null) {
            Ebean.save(buildLogDtl);
        }
    }

    public void update(AdStageLogDtl buildLogDtl) {
        if (buildLogDtl != null) {
            Ebean.update(buildLogDtl);
        }
    }

    public List<AdStageLogDtl> qryStageLogBySeqId(long seq_id) {
        List<AdStageLogDtl> buildLogDtls = new QAdStageLogDtl().totalStep.eq(seq_id).orderBy("STAGE_ID").findList();
        return buildLogDtls;
    }

    public void updateStageLogDtl(AdStageLogDtl buildLogDtl) {
        if (buildLogDtl != null) {
            buildLogDtl.save();
        }
    }

    public AdStageLogDtl qryStageLogByTotalstep(long total_step, long branch_id, long stage_id) {
        AdStageLogDtl adStageLogDtl = new QAdStageLogDtl().totalStep.eq(total_step).adStage.stageId.eq(stage_id).adBranch.branchId.eq(branch_id).findUnique();
        return adStageLogDtl;
    }

    public List<AdStageLogDtl> qryStageLogByTotalstep(long total_step, long branch_id) {
        List<AdStageLogDtl> adStageLogDtlList = new QAdStageLogDtl().adBranch.branchId.eq(branch_id).totalStep.eq(total_step).findList();
        return adStageLogDtlList;
    }

    public List<SqlRow> qryOpUserInLogDtl(Long branchId) {
        return Ebean.createSqlQuery("SELECT DISTINCT T.OP_ID,U.DISPLAY_NAME FROM AD_STAGE_LOG_DTL T" +
            ",AD_USER U WHERE T.STATE = 1 AND U.STATE = 1 AND T.BRANCH_ID = :branchId AND T.OP_ID = U.USER_ID(+) ").setParameter("branchId", branchId).findList();
    }

    public AdStageLogDtl qryByCommitIdAndBranch(String commitId, Long branchId) {
        List<AdStageLogDtl> dtlList = new QAdStageLogDtl().state.eq(1).adStage.stageCode.eq(1)
            .commitId.eq(commitId).adBranch.branchId.eq(branchId).orderBy(" CREATE_DATE DESC").findList();
        if (dtlList != null && dtlList.size() > 0) {
            return dtlList.get(0);
        } else {
            return null;
        }
    }

    public List<AdStageLogDtl> qryAfterStep(Long seqId, int step) {
        return new QAdStageLogDtl().state.eq(1).totalStep.eq(seqId).step.greaterOrEqualTo((long) step).findList();
    }

    public long getSeqByBranchId(Long branchId) {
        PagedList<AdStageLogDtl> dtlPage = new QAdStageLogDtl().adBranch.branchId.eq(branchId).state.eq(1).totalStep.isNotNull().orderBy("TOTAL_STEP DESC").findPagedList(0, 1);
        if (dtlPage != null && dtlPage.getTotalRowCount() > 0) {
            return dtlPage.getList().get(0).getTotalStep();
        } else {
            return 0;
        }
    }

    public List<AdStageLogDtl> qry(long branchId, long stageId) {
        return new QAdStageLogDtl().adBranch.branchId.eq(branchId).state.eq(1).totalStep.isNotNull().adStage.stageId.eq(stageId)
            .orderBy("TOTAL_STEP DESC").findList();
    }

    public List<SqlRow> qryAvgTime(long branchId, long stageId) {
        String sql = " select NVL(avg(finish_date-begin_date)*24*3600,1) avgtime from ad_stage_log_dtl where branch_id=:branchId and stage_id=:stageId\n" +
            "    and state=1 and begin_date is not null and finish_date is not null and stage_result=2\n" +
            "    and rownum<=10 order by log_id desc";
        SqlQuery sqlQuery = createSqlQuery(sql);
        sqlQuery.setParameter("branchId", branchId);
        sqlQuery.setParameter("stageId", stageId);
        return sqlQuery.findList();
    }

    public List<AdStageLogDtl> qryStageLogByStageId(long total_step, long stage_id) {
        return new QAdStageLogDtl().totalStep.eq(total_step).state.eq(1).adStage.stageId.eq(stage_id).orderBy("begin_date desc").findList();
    }

}
