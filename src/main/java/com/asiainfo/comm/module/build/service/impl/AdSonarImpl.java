package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdSonarDAO;
import com.asiainfo.comm.module.models.AdSonarData;
import com.asiainfo.comm.module.models.AdSonarReport;
import com.avaje.ebean.PagedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangpeng on 2016/7/5.
 */
@Component
public class AdSonarImpl {
    @Autowired
    AdSonarDAO sonarDAO;

    public PagedList<AdSonarData> QryAdSonarByProjectName(String project_name) {
        return sonarDAO.QryAdSonarDataByProjectName(project_name);
    }

    public Boolean obtAdSonarData() throws Exception {
        return sonarDAO.obtAdSonarDara();
    }

    public int getCurData() {
        return sonarDAO.getCurData();
    }

    public void test() {
        sonarDAO.test();
    }

    public List<AdSonarData> qryAdSonarByName(String name) {
        return sonarDAO.qryAdSonarByName(name);
    }

    public List<AdSonarReport> getAdSonarReportDataByName(String name, Long type) {
        return sonarDAO.getAdSonarReportDataByName(name, type);
    }

    public List<AdSonarData> qryAdSonarDataById(Long id) {
        return sonarDAO.qryAdSonarDataById(id);
    }

    public Map getWeekDate(String sDate) {
        return sonarDAO.getWeekDate(sDate);
    }

    public Date getEveryDate(String sDate, int day) {
        return sonarDAO.getEveryDate(sDate, day);
    }

    public String getMonthDate(String sDate, int day) {
        return sonarDAO.getMonthDate(sDate, day);
    }
}
