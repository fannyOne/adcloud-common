package com.asiainfo.schedule.sync;

import com.asiainfo.comm.module.build.service.impl.AdSonarImpl;
import com.asiainfo.comm.module.build.service.impl.AdSonarReportImpl;
import com.asiainfo.comm.module.models.AdSonarData;
import com.asiainfo.comm.module.models.AdSonarReport;
import com.avaje.ebean.PagedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by zhangpeng on 2016/7/5.
 */
@lombok.extern.slf4j.Slf4j
@Component
public class SonarScan {
    @Autowired
    private AdSonarImpl adSonar;
    @Autowired
    private AdSonarReportImpl adSonarReports;

    @Async
    public void getSonarsCur() throws Exception { // TODO 获取最近一次的Sonar信息
        log.error("SonarScan scan is running");
        String[] jobName = {"res-parent", "account-center", "aicust-parent", "kt-center", "order-center"};
        Boolean obtData = adSonar.obtAdSonarData();
        if (obtData) {
            log.error("sonar,插入数据成功！！！！！！！！！！！！！！！！！");
            int date = adSonar.getCurData();
            for (int i = 0; i < jobName.length; i++) {
                AdSonarData newData;
                AdSonarData oldData;
                if (date == 1) {//TODO 当前插入为日报
                    log.error("当前插入为日报！！！！！！！！！！！！");
                    PagedList<AdSonarData> adSonarDatas = adSonar.QryAdSonarByProjectName(jobName[i]);
                    log.error(" jobNamei=" + jobName[i] + " adSonarDatas的大小为：" + adSonarDatas.getList().size());
                    if (adSonarDatas.getList() != null && adSonarDatas.getList().size() > 1) {
                        newData = adSonarDatas.getList().get(0);
                        oldData = adSonarDatas.getList().get(1);
                        deldata(newData, oldData, 1L);
                    } else {
                        log.error("该jobname=" + jobName[i] + "数据少于2条");
                    }
                } else if (date == 5) {//TODO 当前插入为周报每周五
                    log.error("当前插入为周报,星期五！！！！！！！！！！！！");
                    List<AdSonarData> adSonarDatas1 = adSonar.qryAdSonarByName(jobName[i]);
                    List<AdSonarReport> adSonarReports = adSonar.getAdSonarReportDataByName(jobName[i], 2L);
                    if (adSonarReports != null && adSonarReports.size() > 0) {
                        List<AdSonarData> adSonarDatas = adSonar.qryAdSonarDataById(adSonarReports.get(0).getAdSonarData().getId());
                        if (adSonarDatas != null && adSonarDatas.size() > 0) {
                            newData = adSonarDatas1.get(0);
                            oldData = adSonarDatas.get(0);
                            deldata(newData, oldData, 2L);
                        }
                    } else {
                        log.error("AD_Sonar_Data 相关的周报数据为空！！！！！");
                    }
                } else if (date == 7) {//TODO 当前插入为周报每周日
                    log.error("当前插入为周报，星期日！！！！！！！！！！！！");
                    List<AdSonarData> adSonarDatas1 = adSonar.qryAdSonarByName(jobName[i]);
                    List<AdSonarReport> adSonarReports = adSonar.getAdSonarReportDataByName(jobName[i], 2L);
                    if (adSonarReports != null && adSonarReports.size() > 1) {
                        System.out.println("AD_Sonar_Data 相关的不为空77777777！！！！！！！！！");
                        List<AdSonarData> adSonarDatas = adSonar.qryAdSonarDataById(adSonarReports.get(1).getAdSonarData().getId());
                        newData = adSonarDatas1.get(0);
                        oldData = adSonarDatas.get(0);
                        updateDate(newData, oldData, adSonarReports.get(0));
                    } else {
                        log.error("AD_Sonar_Data 相关的数据为空！！！！！");
                    }
                } else if (date == 3) {//TODO 当前插入为月报，每月末
                    log.error("当前插入为月报，" + new Date() + "！！！！！！！！！！！！");
                    List<AdSonarData> adSonarDatas = adSonar.qryAdSonarByName(jobName[i]);
                    List<AdSonarReport> adSonarReports = adSonar.getAdSonarReportDataByName(jobName[i], 3L);
                    if (adSonarDatas != null && adSonarDatas.size() > 0) {
                        System.out.println("AD_Sonar_date 月报相关的数据不为空！！！！！！！！！");
                        List<AdSonarData> adSonarData1 = adSonar.qryAdSonarDataById(adSonarReports.get(0).getAdSonarData().getId());
                        if (adSonarData1 != null && adSonarData1.size() > 0) {
                            newData = adSonarDatas.get(0);
                            oldData = adSonarData1.get(0);
                            deldata(newData, oldData, 3L);
                        }
                    } else {
                        log.error("AD_Sonar_date 相关的月报数据为空！！！！！");
                    }

                }
            }
        } else {
            log.error("sonar_data插入数据失败！！！！！！！！！！！！！！！！！！！！！！！！");
        }
//        }
    }

    public void deldata(AdSonarData newData, AdSonarData oldData, Long type) {

        DecimalFormat df = new DecimalFormat("#.0");
        AdSonarReport adSonarReport = new AdSonarReport();
        adSonarReport.setProjectName(newData.getProjectName());
        adSonarReport.setFilenumsC(newData.getFilenums() - oldData.getFilenums());
        adSonarReport.setMethodnumsC(newData.getMethodnums() - oldData.getMethodnums());
        adSonarReport.setCodelinesC(newData.getCodelines() - oldData.getCodelines());
        adSonarReport.setRepeatC(Double.parseDouble(df.format(newData.getRepeat() - oldData.getRepeat())));
        adSonarReport.setMethodCompC(Double.parseDouble(df.format(newData.getMethodComp() - oldData.getMethodComp())));
        adSonarReport.setDComplexityC(newData.getDComplexity() - oldData.getDComplexity());
        adSonarReport.setSeriousIssuesC(newData.getSeriousIssues() - oldData.getSeriousIssues());
        adSonarReport.setBlockIssuesC(newData.getBlockIssues() - oldData.getBlockIssues());
        adSonarReport.setCoverageC(Double.parseDouble(df.format(newData.getCoverage() - oldData.getCoverage())));
        adSonarReport.setUnitSuccessRateC(Double.parseDouble(df.format(newData.getUnitSuccessRate() - oldData.getUnitSuccessRate())));
        adSonarReport.setUnitnumsC(newData.getUnitnums() - oldData.getUnitnums());
        adSonarReport.setUnittimeC(newData.getUnittime() - oldData.getUnittime());
        adSonarReport.setScanDate(new Date());
        adSonarReport.setReports(type);
        adSonarReport.setAdSonarData(newData);
        adSonarReports.getAdSonarReportByDateAndType(newData.getProjectName(), type);
        adSonarReport.save();
    }

    public void updateDate(AdSonarData newData, AdSonarData oldData, AdSonarReport adSonarReport) {
        adSonarReports.delBySonarReptId(adSonarReport.getId());
        AdSonarReport sonarReport = new AdSonarReport();
        DecimalFormat df = new DecimalFormat("#.0");
        sonarReport.setProjectName(newData.getProjectName());
        sonarReport.setFilenumsC(newData.getFilenums() - oldData.getFilenums());
        sonarReport.setMethodnumsC(newData.getMethodnums() - oldData.getMethodnums());
        sonarReport.setCodelinesC(newData.getCodelines() - oldData.getCodelines());
        sonarReport.setRepeatC(Double.parseDouble(df.format(newData.getRepeat() - oldData.getRepeat())));
        sonarReport.setMethodCompC(newData.getMethodComp() - oldData.getMethodComp());
        sonarReport.setDComplexityC(newData.getDComplexity() - oldData.getDComplexity());
        sonarReport.setSeriousIssuesC(newData.getSeriousIssues() - oldData.getSeriousIssues());
        sonarReport.setBlockIssuesC(newData.getBlockIssues() - oldData.getBlockIssues());
        sonarReport.setCoverageC(newData.getCoverage() - oldData.getCoverage());
        sonarReport.setUnitSuccessRateC(Double.parseDouble(df.format(newData.getUnitSuccessRate() - oldData.getUnitSuccessRate())));
        sonarReport.setUnitnumsC(newData.getUnitnums() - oldData.getUnitnums());
        sonarReport.setUnittimeC(Double.parseDouble(df.format((newData.getUnittime() - oldData.getUnittime()) / 1000)));
        sonarReport.setScanDate(new Date());
        sonarReport.setReports(2L);
        sonarReport.setAdSonarData(newData);
        sonarReport.save();
    }

}


