package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdBuildDeployReportDAO;
import com.asiainfo.comm.module.models.AdBuildDeployData;
import com.avaje.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhangpeng on 2016/7/12.
 */
@Component
public class AdBuildDeployReportImpl {
    @Autowired
    AdBuildDeployReportDAO adBuildDeployReportDAO;

    public List<AdBuildDeployData> getWeekData(long id) {
        return adBuildDeployReportDAO.getWeekData(id);
    }

    public List<SqlRow> getMonthData(String name, String date) {
        return adBuildDeployReportDAO.getMonthData(name, date);
    }

    public int getLastDayOfMonth(int year, int month) {//TODO 获取某年某月的最后一天
        return adBuildDeployReportDAO.getLastDayOfMonth(year, month);
    }

    public List<SqlRow> getBuildDeployReportTenData(String project_name, String date) {
        return adBuildDeployReportDAO.getBuildDeployReportTenData(project_name, date);
    }

    public List<SqlRow> getMonthyDataByDate(String name, String date) {//TODO 根据日期获取相应的月报
        return adBuildDeployReportDAO.getMonthyDataByDate(name, date);
    }
}
