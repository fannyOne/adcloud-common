package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdBuildDeployDataDAO;
import com.asiainfo.comm.module.models.AdBuildDeployReport;
import com.avaje.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangpeng on 2016/7/12.
 */
@Component
public class AdBuildDeployDataImpl {
    @Autowired
    AdBuildDeployDataDAO adBuildDeployDataDAO;

    public void getBuildLogDeployData(long projectId) {
        adBuildDeployDataDAO.getBuildLogDeployData(projectId);
    }

    public List<SqlRow> getWeekReportDataByDate(String project_name, Long type, String date) {
        return adBuildDeployDataDAO.getWeekReportDataByDate(project_name, type, date);
    }

    public List<AdBuildDeployReport> getBuildDeployDateById(Long id) {
        return adBuildDeployDataDAO.getBuildDeployDateById(id);
    }

    public List<Map<String, String>> qryBuildLogReport(String beginDate, String endDate, String groupIds, int qryType) {

        return adBuildDeployDataDAO.qryBuildLogReport(beginDate, endDate, groupIds, qryType);
    }

    public List<Map<String, Object>> qryBuildLogReportV2(String beginDate, String endDate, String groupIds, int qryType) {

        return adBuildDeployDataDAO.qryBuildLogReportV2(beginDate, endDate, groupIds, qryType);
    }
}
