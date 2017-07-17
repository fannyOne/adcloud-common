package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoMaster.AdWorkspaceAddPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.ManualHandPojo;
import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdBuildLogDAO;
import com.asiainfo.comm.module.build.dao.impl.AdGroupDAO;
import com.asiainfo.comm.module.build.dao.impl.AdUserBranchDAO;
import com.asiainfo.comm.module.busiLog.dao.impl.AdProjectCodeReportDAO;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.role.dao.impl.AdUserRoleRelDAO;
import com.asiainfo.util.DateConvertUtils;
import com.avaje.ebean.SqlRow;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/1 0001.
 */
@Component("AdBranchImpl")
public class AdBranchImpl {
    @Autowired
    JenkinsImpl jenkinsService;
    @Autowired
    AdBranchDAO branchDAO;
    @Autowired
    AdUserBranchDAO userBranchDAO;
    @Autowired
    AdUserRoleRelDAO userRoleRelDAO;
    @Autowired
    AdUserImpl userImpl;
    @Autowired
    AdBuildLogImpl buildLogImpl;
    @Autowired
    AdBuildReturnValueImpl returnValueImpl;
    @Autowired
    AdStageImpl stageImpl;
    @Autowired
    AdPipeLineStateImpl pipeLineStateImpl;
    @Autowired
    JenkinsImpl jenkinsImpl;
    @Autowired
    AdGroupDAO adGroupDAO;
    @Autowired
    AdBuildLogDAO adBuildLogDAO;
    @Autowired
    AdBranchDAO adBranchDAO;
    @Autowired
    AdProjectCodeReportDAO adProjectCodeReportDAO;


    public AdBranch qryById(long branchId) {
        return branchDAO.qryById(branchId);
    }

    public List<AdBranch> qryAllBranch() {
        return branchDAO.getAllEnv();
    }

    public List<AdBranch> qryAllBranchInfo(int pageNum, int pageSize, String[] projects) {
        return branchDAO.getAllBranchInfo(pageNum, pageSize, projects);
    }

    public List<AdBranch> qryBranchByProject(long projectId) {
        return branchDAO.qryBranchByProject(projectId);
    }

    public List<AdBranch> qryBranchByProjects(String[] projectId) {
        return branchDAO.qryBranchByProjects(projectId);
    }

    public void addBranchToWorkSpace(AdWorkspaceAddPojo poj, Long branchId, Long userId, String username) {
        boolean isAdmin = false;
        //查询角色列表
        List<AdUserRoleRel> userRoleRelList = userRoleRelDAO.qryByUser(username);
        for (AdUserRoleRel rel : userRoleRelList) {
            if (rel.getAdRole().getRoleLevel() == 0) {
                isAdmin = true;
                break;
            }
        }
        int count;
        if (isAdmin) {
            count = userBranchDAO.qryNumByBranchIdAndAdminUserName(branchId, username);
        } else {
            count = userBranchDAO.qryNumByBranchIdAndUserName(branchId, username);
        }
        if (count == 0) {
            AdBranch branch = branchDAO.qryBranchByid(branchId);
            userBranchDAO.addBranchToWorkSpace(branch, userId, username);
            poj.setFlag(true);
            poj.setName(branch.getAdProject().getProjectName() + "-" + branch.getBranchDesc());
            if (!isAdmin) {
                userBranchDAO.updateNoRoleData(username);
            }
        }
    }

    public long countAll() {
        return branchDAO.countAll();
    }

    public long countLastMonth() {
        return branchDAO.countCreateDate(DateConvertUtils.getStartTimeInMonth());
    }

    public long countAllEvn() {
        List<SqlRow> sqlrows = branchDAO.countAllEnv();
        if (CollectionUtils.isEmpty(sqlrows)) {
            return 0;
        }
        return sqlrows.get(0).getLong("num");
    }

    public long countLastMonthEnv() {
        List<SqlRow> sqlrows = branchDAO.countEnvCreateDate(DateConvertUtils.getStartTimeInMonth());
        if (CollectionUtils.isEmpty(sqlrows)) {
            return 0;
        }
        return sqlrows.get(0).getLong("num");
    }


    public ManualHandPojo beginStage(long step, long userId, long branchId, AdStage restartStage, int busiType) throws Exception { //busiType 3:重启 4:重新部署
        System.out.println("userId:" + userId);
        ManualHandPojo poj;
        AdBranch adBranch = restartStage.getAdBranch();
        AdUser adUser = userImpl.qryById(userId);
        AdPipeLineState pipeLineState = restartStage.getAdPipeLineState();
        Long serialNumber = null;
        String seqNumStr = stageImpl.UpdateAdOperationByEnvId("" + branchId, "0");//开始状态置为0
        if (StringUtils.isNotEmpty(seqNumStr)) {
            serialNumber = Long.valueOf(seqNumStr);
        }
        if (pipeLineState != null) {
            pipeLineState.setLastBuildResult(0);
            pipeLineState.setBranchState(2);
            pipeLineState.setBuildType(busiType);
            pipeLineStateImpl.updatePipeLineState(pipeLineState);
        }
        AdBuildLog adBuildLog = new AdBuildLog();
        adBuildLog.setBuildSeq(step);
        adBuildLog.setAdUser(adUser);
        adBuildLog.setBuildType(busiType);
        adBuildLog.setCreateDate(new java.util.Date());
        adBuildLog.setAdBranch(adBranch);
        adBuildLog.setBuildResult(0);
        adBuildLog.setState(1);
        adBuildLog.setLastStep((int) step);
        if (serialNumber != null) {
            adBuildLog.setTotalStep((int) serialNumber.longValue());
        }
        buildLogImpl.insertBuildLog(adBuildLog);
        //记录seqId
        AdBuildReturnValue adBuildReturnValue = null;
        if (pipeLineState != null) {
            adBuildReturnValue = returnValueImpl.qryBuildReturnValue(pipeLineState.getPipelineId(), step);
        }
        if (adBuildReturnValue == null) {
            adBuildReturnValue = new AdBuildReturnValue();
            adBuildReturnValue.setPipelineId(pipeLineState.getPipelineId());
            adBuildReturnValue.setBuildSeq(serialNumber);
            adBuildReturnValue.setStep(step);
            adBuildReturnValue.setNext_step(step + 1);
            returnValueImpl.insertBuildReturnSeq(adBuildReturnValue);
        } else {
            adBuildReturnValue.setBuildSeq(serialNumber);
            returnValueImpl.updateBuildReturnSeq(adBuildReturnValue);
        }
        //启动构建
        poj = jenkinsImpl.triggerJenkins(adBranch, restartStage);
        if (!poj.getRetCode().equals("200")) {
            pipeLineState.setBranchState(1);
            pipeLineState.setLastBuildResult(3);
            pipeLineStateImpl.updatePipeLineState(pipeLineState);
            stageImpl.updateState(restartStage, 3);
            throw new Exception("触发Jenkins失败");
        }
        return poj;
    }

    public List<SqlRow> qryBranchByProjectId(long projectId) {
        return branchDAO.qryBranchByProjectId(projectId);
    }

    /**
     * 获取有CRON任务的流水
     */
    public List<AdBranch> qryBranchCronExsit() {
        return adBranchDAO.qryBranchCronExsit();
    }
}
