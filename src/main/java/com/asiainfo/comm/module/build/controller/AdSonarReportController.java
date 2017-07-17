package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.common.pojo.pojoExt.*;
import com.asiainfo.comm.common.pojo.pojoMaster.*;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.comm.module.build.service.impl.*;
import com.asiainfo.comm.module.busiLog.service.impl.ExcelOutputServiceImpl;
import com.asiainfo.comm.module.models.AdBuildDeployData;
import com.asiainfo.comm.module.models.AdBuildDeployReport;
import com.asiainfo.comm.module.models.AdSonarData;
import com.asiainfo.comm.module.models.AdSonarReport;
import com.asiainfo.comm.module.role.controller.BaseController;
import com.avaje.ebean.SqlRow;
import jxl.write.WritableWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhangpeng on 2016/7/7.
 */
@lombok.extern.slf4j.Slf4j
@RestController
public class AdSonarReportController extends BaseController {
    @Autowired
    AdSonarImpl adSonar;
    @Autowired
    AdSonarReportImpl adSonarReport;
    @Autowired
    AdBuildDeployReportImpl adBuildDeployReport;
    @Autowired
    AdBuildDeployDataImpl adBuildDeployData;
    @Autowired
    AdUserDataRelateImpl userDataRelateImpl;
    @Autowired
    ExcelOutputServiceImpl excelOutputService;
    @Autowired
    AdTreeDataImpl treeDataImpl;

    public Map<String, String> projectNames() {
        Map<String, String> builderType = new HashMap<>();
        builderType.put("res-parent", "资源中心");
        builderType.put("aicust-parent", "客管中心");
        builderType.put("account-center", "账户中心");
        builderType.put("kt-center", "开通中心");
        builderType.put("order-center", "订单中心");
        builderType.put("ADCloud_BE", "AdCloud");
        return builderType;
    }

    public Map<String, String> projectNamesNew() {
        Map<String, String> builderType = new HashMap<>();
        builderType.put("res-center", "资源中心");
        builderType.put("aicust-center", "客管中心");
        builderType.put("account-center", "账户中心");
        builderType.put("kt-center", "开通中心");
        builderType.put("order-center", "订单中心");
        return builderType;
    }

    @RequestMapping(value = "/sonarReport", produces = "application/json")
    public String getSonarReport(@RequestParam Map map) {
        String[] jobName = {"res-parent", "account-center", "aicust-parent", "kt-center", "order-center"};
        Map<String, String> builderType = projectNames();
        Map<String, String> dateMap = adSonar.getWeekDate((String) map.get("date"));//TODO 获取传入日期的周数
        String isCenterSys = (String) map.get("isCenterSys");
        List<SqlRow> sqlRowList;
        List<AdSonarReport> adSonarReportsPage;
        List<AdSonarData> adSonarDatas;
        AdSonarReport adReport;
        AdSonarData adSonarData;
        List<SonarReportExtPojo> sonarReportExtPojoList = new ArrayList<>();
        AdSonarReportPojo adSonarReportPojo = new AdSonarReportPojo();
        String mdate;
        String endDate = "";
        int weekDate = 0;
        int monthDate = 0;
        DecimalFormat df = new DecimalFormat("#.0");
        if (isCenterSys.equals("true")) {
            for (int i = 0; i < jobName.length; i++) {
                SonarReportExtPojo reportExtPojo = new SonarReportExtPojo();
                System.out.println("map.get(type)====" + Long.valueOf((String) map.get("type")));
                if (Long.valueOf((String) map.get("type")) == 2) {//周报
                    sqlRowList = adSonarReport.getAdSonarReportByNameAndDate(jobName[i], 2L, dateMap.get("sunday"));//根据日期查询当周的周报或日报
                    endDate = dateMap.get("sunday").split("-")[1] + "." + dateMap.get("sunday").split("-")[2];
                    System.out.println("dateMap.get(sunday)====" + dateMap.get("sunday") + "无数据");
                    if (sqlRowList.size() == 0) {
                        sqlRowList = adSonarReport.getAdSonarReportByNameAndDate(jobName[i], 2L, dateMap.get("friday"));//根据日期查询当周的周报或日报
                        endDate = dateMap.get("friday").split("-")[1] + "." + dateMap.get("friday").split("-")[2];
                        if (sqlRowList.size() == 0) {
                            System.out.println("dateMap.get(friday)====" + dateMap.get("friday") + "无数据");
                            reportExtPojo.setEnvName(builderType.get(jobName[i]));
                            reportExtPojo.setDate(dateMap.get("friday"));
                            setAdSonarReport(reportExtPojo, builderType.get(jobName[i]), dateMap.get("friday"), sonarReportExtPojoList);
                            weekDate++;
                            continue;
                        }
                    }
                    adSonarReportsPage = adSonarReport.qryById(sqlRowList.get(0).getLong("id"));
                } else {//月报
                    mdate = adSonar.getMonthDate((String) map.get("date"), 0);
                    endDate = mdate.split("-")[1];
                    log.error("format.format((Date)map.get(date))====" + mdate);
                    sqlRowList = adSonarReport.getAdReportByNameAndDate(jobName[i], Long.valueOf((String) map.get("type")), mdate);
                    if (sqlRowList.size() == 0) {
                        setAdSonarReport(reportExtPojo, builderType.get(jobName[i]), mdate, sonarReportExtPojoList);
                        monthDate++;
                        continue;
                    }
                    adSonarReportsPage = adSonarReport.qryById(sqlRowList.get(0).getLong("id"));
                }
                if (adSonarReportsPage != null && adSonarReportsPage.size() > 0) {
                    adReport = adSonarReportsPage.get(0);
                    adSonarDatas = adSonarReport.obtSonarDataSonarId(adReport.getAdSonarData().getId());//根据周报或月报的sonarID查询sonarDate表里的数据
                    adSonarData = adSonarDatas.get(0);
                    reportExtPojo.setEnvName(builderType.get(jobName[i]));
                    reportExtPojo.setDate(new SimpleDateFormat("yyyy-MM-dd").format(adSonarData.getScanDate()));
                    reportExtPojo.setFileNum(new SonarValueExtPojo(adSonarData.getFilenums(), adReport.getFilenumsC()));
                    reportExtPojo.setBlockPro(new SonarValueExtPojo(adSonarData.getBlockIssues(), adReport.getBlockIssuesC()));
                    reportExtPojo.setCodeCover(new SonarValueExtPojo(adSonarData.getCoverage(), Double.parseDouble(df.format(adReport.getCoverageC()))));
                    reportExtPojo.setCodeLine(new SonarValueExtPojo(adSonarData.getCodelines(), adReport.getCodelinesC()));
                    reportExtPojo.setMethodComp(new SonarValueExtPojo(adSonarData.getMethodComp(), Double.parseDouble(df.format(adReport.getMethodCompC()))));
                    reportExtPojo.setRepeatRate(new SonarValueExtPojo(adSonarData.getRepeat(), adReport.getRepeatC()));
                    reportExtPojo.setMethodNum(new SonarValueExtPojo(adSonarData.getMethodnums(), adReport.getMethodnumsC()));
                    reportExtPojo.setSeriousPro(new SonarValueExtPojo(adSonarData.getSeriousIssues(), adReport.getSeriousIssuesC()));
                    reportExtPojo.setTotalComp(new SonarValueExtPojo(adSonarData.getDComplexity(), adReport.getDComplexityC()));
                    reportExtPojo.setUnitNum(new SonarValueExtPojo(adSonarData.getUnitnums(), adReport.getUnitnumsC()));
                    reportExtPojo.setUnitSuc(new SonarValueExtPojo(adSonarData.getUnitSuccessRate(), Double.parseDouble(df.format(adReport.getUnitSuccessRateC()))));
                    reportExtPojo.setBlockPro(new SonarValueExtPojo(adSonarData.getBlockIssues(), adReport.getBlockIssuesC()));
                    reportExtPojo.setUnitTime(new SonarValueExtPojo(adSonarData.getUnittime(), Double.parseDouble(df.format(adReport.getUnittimeC()))));
                    sonarReportExtPojoList.add(reportExtPojo);
                } else {
                    log.error("该项目:" + jobName[i] + "没有soanr数据");
                }
            }
            if (Long.valueOf((String) map.get("type")) == 2) {
                adSonarReportPojo.setDates(dateMap.get("monday").split("-")[1] + "." + dateMap.get("monday").split("-")[2] + "---" + endDate + "周报");
                if (weekDate == jobName.length) {
                    return JsonpUtil.modelToJson(adSonarReportPojo);
                }
            } else {
                adSonarReportPojo.setDates(endDate + "月报");
                if (monthDate == jobName.length) {
                    return JsonpUtil.modelToJson(adSonarReportPojo);
                }
            }
            adSonarReportPojo.setData(sonarReportExtPojoList);
            return JsonpUtil.modelToJson(adSonarReportPojo);
        } else {
            System.out.print("不是中心化项目");
            return null;
        }
    }

    @RequestMapping(value = "/buildDeployMonthyReport", produces = "application/json")
    public String buildDeployMonthyReport(@RequestParam Map map) {//TODO 获取构建部署成功次数月报
        int count = 0;
        int weekDate = 0;
        String date = (String) map.get("date");
        String isCenterSys = (String) map.get("isCenterSys");
        DecimalFormat df = new DecimalFormat("#.0");
        String[] jobName = {"res-center", "account-center", "aicust-center", "kt-center", "order-center"};
        Map<String, String> builderType = projectNamesNew();
        AdBuildDeployReportPojo adBuildDeployReportPojo = new AdBuildDeployReportPojo();
        List<SqlRow> sqlRowList;
        List<BuildDeployExtPojo> buildDeployExtPojos = new ArrayList<>();
        List<BuildDeployDateExtPojo> buildDeployDateExtPojos = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("MM.dd");
        String mdate = adSonar.getMonthDate(date, 0);//获取传入的月份
        //String firstDay=adBuildDeployReport.getMonthlyFirstDay(adSonar.getMonthDate(date,-1));//获取上个月的第一天
        String firstDay = adSonar.getMonthDate(date, -1) + "-01";
        System.out.println("lastFirstDay====" + firstDay);
        System.out.print(mdate.split("-").length + "+++++" + Integer.parseInt((mdate.split("-")[0])) + "+++++++" + Integer.parseInt(mdate.split("-")[1]));
        int sqlDay;
        int lastDay = adBuildDeployReport.getLastDayOfMonth(Integer.parseInt((mdate.split("-")[0])), Integer.parseInt((mdate.split("-")[1])));
        if (isCenterSys.equals("true")) {
            for (int i = 0; i < jobName.length; i++) {
                count++;
                BuildDeployExtPojo buildDeployExtPojo = new BuildDeployExtPojo();
                sqlRowList = adBuildDeployReport.getMonthyDataByDate(jobName[i], date);//TODO 获取最近一条月报
                List<SqlRow> beforList = adBuildDeployReport.getMonthyDataByDate(jobName[i], firstDay);//TODO 获取上个月的月报
                List<SqlRow> list = adBuildDeployReport.getMonthData(jobName[i], mdate);//TODO 获取这个月每天的信息
                int listCount = list.size();
                List<BuildSucPresExtPojo> BuildSucPresExtPojoList = new ArrayList<>();
                List<DeploySucPresExtPojo> DeploySucPresExtPojoList = new ArrayList<>();
                for (int day = 1; day <= lastDay; day++) {
                    BuildDeployDateExtPojo buildDeployDateExtPojo = new BuildDeployDateExtPojo();
                    BuildSucPresExtPojo BuildSucPresExtPojo = new BuildSucPresExtPojo();
                    DeploySucPresExtPojo deploySucPresExtPojo = new DeploySucPresExtPojo();
                    if (count == 1) {
                        buildDeployDateExtPojo.setDate(mdate.split("-")[1] + "." + day);
                        buildDeployDateExtPojos.add(buildDeployDateExtPojo);
                    }
                    if (sqlRowList.size() != 0 && listCount != 0) {
                        sqlDay = Integer.parseInt(format.format(list.get(listCount - 1).getDate("create_time")).split("\\.")[1]);
                        log.error("day=====================" + day + "sqlDay====================" + sqlDay);
                        if (day == sqlDay) {

                            log.error("当月的数据不为0:222222222=================================================" + day);
                            BuildSucPresExtPojo.setBuildSucPre(Double.parseDouble(df.format((list.get(listCount - 1).getDouble("b_success_num") / (list.get(listCount - 1).getDouble("b_success_num") + list.get(listCount - 1).getDouble("b_fail_num") + 0.0000000001)) * 100)));
                            BuildSucPresExtPojoList.add(BuildSucPresExtPojo);
                            deploySucPresExtPojo.setDeploySucPre(Double.parseDouble(df.format((list.get(listCount - 1).getDouble("d_success_num") / (list.get(listCount - 1).getDouble("d_success_num") + list.get(listCount - 1).getDouble("d_fail_num") + 0.0000000001)) * 100)));
                            DeploySucPresExtPojoList.add(deploySucPresExtPojo);
                            buildDeployExtPojo.setBuildSucPres(BuildSucPresExtPojoList);
                            buildDeployExtPojo.setDeploySucPres(DeploySucPresExtPojoList);
                            listCount--;
                            continue;
                        }
                    }
                    System.out.println("4444444444444444444444=================================================" + day);
                    BuildSucPresExtPojo.setBuildSucPre(-1);
                    BuildSucPresExtPojoList.add(BuildSucPresExtPojo);
                    deploySucPresExtPojo.setDeploySucPre(-1);
                    DeploySucPresExtPojoList.add(deploySucPresExtPojo);
                    buildDeployExtPojo.setBuildSucPres(BuildSucPresExtPojoList);
                    buildDeployExtPojo.setDeploySucPres(DeploySucPresExtPojoList);
                }
                buildDeployExtPojo.setProjectName(builderType.get(jobName[i]));
                if (sqlRowList.size() == 0) {
                    buildDeployExtPojo.setBuildSucPre(0d);
                    buildDeployExtPojo.setDeploySucPre(0d);
                    buildDeployExtPojo.setBuildSucPreHis(0d);
                    buildDeployExtPojo.setDeploySucPreHis(0d);
                    weekDate++;
                } else if (sqlRowList.get(0).getDouble("b_success_rate") != 0) {
                    buildDeployExtPojo.setBuildSucPre(sqlRowList.get(0).getDouble("b_success_rate"));
                    buildDeployExtPojo.setDeploySucPre(sqlRowList.get(0).getDouble("d_success_rate"));
                    if (beforList.size() != 0) {
                        buildDeployExtPojo.setBuildSucPreHis(Double.parseDouble(df.format(((sqlRowList.get(0).getDouble("b_success_rate") - beforList.get(0).getDouble("b_success_rate")) / sqlRowList.get(0).getDouble("b_success_rate")) * 100)));
                        if (sqlRowList.get(0).getDouble("d_success_rate") != 0) {
                            buildDeployExtPojo.setDeploySucPreHis(Double.parseDouble(df.format(((sqlRowList.get(0).getDouble("d_success_rate") - beforList.get(0).getDouble("d_success_rate")) / sqlRowList.get(0).getDouble("d_success_rate")) * 100)));
                        } else {
                            buildDeployExtPojo.setDeploySucPreHis(0d);
                        }
                    } else {
                        buildDeployExtPojo.setBuildSucPreHis(100d);
                        buildDeployExtPojo.setDeploySucPreHis(100d);
                    }

                } else {
                    buildDeployExtPojo.setBuildSucPre(sqlRowList.get(0).getDouble("b_success_rate"));
                    buildDeployExtPojo.setDeploySucPre(sqlRowList.get(0).getDouble("d_success_rate"));
                    buildDeployExtPojo.setBuildSucPreHis(0d);
                    if (beforList.size() != 0) {
                        if (sqlRowList.get(0).getDouble("d_success_rate") != 0) {
                            buildDeployExtPojo.setDeploySucPreHis(Double.parseDouble(df.format(((sqlRowList.get(0).getDouble("d_success_rate") - beforList.get(0).getDouble("d_success_rate")) / sqlRowList.get(0).getDouble("d_success_rate")) * 100)));
                        } else {
                            buildDeployExtPojo.setDeploySucPreHis(0d);
                        }
                    } else {
                        buildDeployExtPojo.setDeploySucPreHis(100d);
                    }
                }
                buildDeployExtPojos.add(buildDeployExtPojo);
            }
            if (weekDate == jobName.length) {
                return null;
            }
            adBuildDeployReportPojo.setDates(buildDeployDateExtPojos);
            adBuildDeployReportPojo.setReports(buildDeployExtPojos);
            return JsonpUtil.modelToJson(adBuildDeployReportPojo);
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/buildDeployWeekReport", produces = "application/json")
    public String getBuildDeployWeekReport(@RequestParam Map map) {//TODO 获取构建部署成功次数周报
        int count = 0;
        int weekDate = 0;
        String date = (String) map.get("date");
        String isCenterSys = (String) map.get("isCenterSys");
        DecimalFormat df = new DecimalFormat("#.0");
        String[] jobName = {"res-center", "account-center", "aicust-center", "kt-center", "order-center"};
        Map<String, String> builderType = projectNamesNew();
        Date wDate;
        Map<String, String> dateMap = adSonar.getWeekDate(date);//TODO 获取传入日期的周数
        List<AdBuildDeployReport> adBuildDeployReportList;
        List<AdBuildDeployData> adBuildDeployDataList;
        List<SqlRow> sqlRowList;
        AdBuildDeployWeekPojo adBuildDeployWeekPojo = new AdBuildDeployWeekPojo();
        List<BuildDeployWeekDateExtPojo> buildDeployWeekDateExtPojos = new ArrayList<>();
        List<BuildDeployDateExtPojo> buildDeployDateExtPojos = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("MM.dd");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (isCenterSys.equals("true")) {
            for (int i = 0; i < jobName.length; i++) {
                count++;
                List<BuildWeekSUcPresExtPojo> buildWeekSUcPresExtPojoList = new ArrayList<>();
                List<BuildWeekSUcPresExtPojo> deployWeekSUcPresExtPojoList = new ArrayList<>();
                BuildDeployWeekDateExtPojo buildDeployWeekDateExtPojo = new BuildDeployWeekDateExtPojo();
                sqlRowList = adBuildDeployData.getWeekReportDataByDate(jobName[i], 1L, dateMap.get("friday"));//根据日期获取相应的周报
                if (sqlRowList.size() == 0) {
                    weekDate++;
                    for (int day = 0; day < 5; day++) {
                        BuildWeekSUcPresExtPojo buildWeekSUcPresExtPojo = new BuildWeekSUcPresExtPojo();
                        BuildWeekSUcPresExtPojo deployWeekSUcPresExtPojo = new BuildWeekSUcPresExtPojo();
                        BuildDeployDateExtPojo buildDeployDateExtPojo = new BuildDeployDateExtPojo();
                        wDate = adSonar.getEveryDate(dateMap.get("monday"), day);
                        if (count == 1) {
                            buildDeployDateExtPojo.setDate(format.format(wDate));
                            buildDeployDateExtPojos.add(buildDeployDateExtPojo);
                        }
                        buildWeekSUcPresExtPojo.setSuccess(-1);
                        buildWeekSUcPresExtPojo.setFailed(-1);
                        buildWeekSUcPresExtPojoList.add(buildWeekSUcPresExtPojo);
                        deployWeekSUcPresExtPojo.setFailed(-1);
                        deployWeekSUcPresExtPojo.setSuccess(-1);
                        deployWeekSUcPresExtPojoList.add(deployWeekSUcPresExtPojo);
                    }
                    buildDeployWeekDateExtPojo.setBuildSucPre(0d);
                    buildDeployWeekDateExtPojo.setDeploySucPre(0d);
                    buildDeployWeekDateExtPojo.setBuildBuildAve(0d);
                    buildDeployWeekDateExtPojo.setBuildDeployAve(0d);
                } else {
                    adBuildDeployReportList = adBuildDeployData.getBuildDeployDateById(sqlRowList.get(0).getLong("id"));
                    adBuildDeployDataList = adBuildDeployReport.getWeekData(adBuildDeployReportList.get(0).getId());
                    int listCount = adBuildDeployDataList.size();

                    for (int day = 0; day < 5; day++) {
                        BuildWeekSUcPresExtPojo buildWeekSUcPresExtPojo = new BuildWeekSUcPresExtPojo();
                        BuildWeekSUcPresExtPojo deployWeekSUcPresExtPojo = new BuildWeekSUcPresExtPojo();
                        BuildDeployDateExtPojo buildDeployDateExtPojo = new BuildDeployDateExtPojo();
                        wDate = adSonar.getEveryDate(dateMap.get("monday"), day);
                        if (count == 1) {
                            buildDeployDateExtPojo.setDate(format.format(wDate));
                            buildDeployDateExtPojos.add(buildDeployDateExtPojo);
                        }

                        if (listCount != 0) {
                            log.error("day=====================" + day + "wDate===" + yearFormat.format(wDate) + "riq==" + yearFormat.format(adBuildDeployDataList.get(listCount - 1).getCreateTime()));
                            if (yearFormat.format(wDate).equals(yearFormat.format(adBuildDeployDataList.get(listCount - 1).getCreateTime()))) {//判断当天有没有数据
                                buildWeekSUcPresExtPojo.setSuccess(adBuildDeployDataList.get(listCount - 1).getBSuccessNum());
                                buildWeekSUcPresExtPojo.setFailed(adBuildDeployDataList.get(listCount - 1).getBFailNum());
                                buildWeekSUcPresExtPojoList.add(buildWeekSUcPresExtPojo);
                                deployWeekSUcPresExtPojo.setFailed(adBuildDeployDataList.get(listCount - 1).getDFailNum());
                                deployWeekSUcPresExtPojo.setSuccess(adBuildDeployDataList.get(listCount - 1).getDSuccessNum());
                                deployWeekSUcPresExtPojoList.add(deployWeekSUcPresExtPojo);
                                listCount--;
                                continue;
                            }
                        }
                        buildWeekSUcPresExtPojo.setSuccess(-1);
                        buildWeekSUcPresExtPojo.setFailed(-1);
                        buildWeekSUcPresExtPojoList.add(buildWeekSUcPresExtPojo);
                        deployWeekSUcPresExtPojo.setFailed(-1);
                        deployWeekSUcPresExtPojo.setSuccess(-1);
                        deployWeekSUcPresExtPojoList.add(deployWeekSUcPresExtPojo);
                    }
                    buildDeployWeekDateExtPojo.setBuildSucPre(Double.parseDouble(df.format(adBuildDeployReportList.get(0).getBSuccessRate())));
                    buildDeployWeekDateExtPojo.setDeploySucPre(Double.parseDouble(df.format(adBuildDeployReportList.get(0).getDSuccessRate())));
                    buildDeployWeekDateExtPojo.setBuildBuildAve(Double.parseDouble(df.format(adBuildDeployReportList.get(0).getBAverageTimes())));
                    buildDeployWeekDateExtPojo.setBuildDeployAve(Double.parseDouble(df.format(adBuildDeployReportList.get(0).getDAverageTimes())));
                }
                buildDeployWeekDateExtPojo.setProjectName(builderType.get(jobName[i]));
                buildDeployWeekDateExtPojo.setBuild(buildWeekSUcPresExtPojoList);
                buildDeployWeekDateExtPojo.setDeploy(deployWeekSUcPresExtPojoList);
                buildDeployWeekDateExtPojos.add(buildDeployWeekDateExtPojo);
            }
            adBuildDeployWeekPojo.setDates(buildDeployDateExtPojos);
            adBuildDeployWeekPojo.setReports(buildDeployWeekDateExtPojos);
            if (weekDate == jobName.length) {
                return null;
            }

            return JsonpUtil.modelToJson(adBuildDeployWeekPojo);
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/buildDeployTimesReport", produces = "application/json")
    public String getbuildDeployTimesReport(@RequestParam Map map) {
        int count = 0;
        int sqlDay;
        int weekDate = 0;
        int monthDate = 0;
        String isCenterSys = (String) map.get("isCenterSys");
        Map<String, String> dateMap = adSonar.getWeekDate((String) map.get("date"));//TODO 获取传入日期的周数
        String mapDate;
        String begin_date;
        String end_date;
        String firstDay = adSonar.getMonthDate((String) map.get("date"), -1) + "-01";//获取上个月的第一天
        String mdate = adSonar.getMonthDate((String) map.get("date"), 0);//获取传入的月份
        System.out.print(mdate.split("-").length + "qqqq" + Integer.parseInt((mdate.split("-")[0])) + "wwwwwww" + Integer.parseInt(mdate.split("-")[1]));
        int lastDay = adBuildDeployReport.getLastDayOfMonth(Integer.parseInt((mdate.split("-")[0])), Integer.parseInt((mdate.split("-")[1])));
        DecimalFormat df = new DecimalFormat("#.0");
        String[] jobName = {"res-center", "account-center", "aicust-center", "kt-center", "order-center"};
        Map<String, String> builderType = projectNamesNew();
        SimpleDateFormat format = new SimpleDateFormat("MM.dd");
        SimpleDateFormat yarformat = new SimpleDateFormat("yyyy-MM-dd");
        AdBuildDeployTimesPojo adBuildDeployTimesPojo = new AdBuildDeployTimesPojo();
        List<BuildDeployDateExtPojo> buildDeployDateExtPojoList = new ArrayList<>();
        List<BuildDeployTimesExtPojo> buildDeployTimesExtPojoList = new ArrayList<>();
        log.error("type====" + Long.valueOf((String) map.get("type")));
        if (isCenterSys.equals("true")) {
            if (Long.valueOf((String) map.get("type")) == 1) { //TODO 计算周报
                for (int i = 0; i < jobName.length; i++) {
                    count++;
                    List<BuildDeployTimesValueExtPojo> buildTimesList = new ArrayList<>();
                    List<BuildDeployTimesValueExtPojo> deployTimesList = new ArrayList<>();
                    BuildDeployTimesExtPojo buildDeployTimesExtPojo = new BuildDeployTimesExtPojo();
                    List<SqlRow> adBuildDeployReportPagedList = adBuildDeployReport.getBuildDeployReportTenData(jobName[i], dateMap.get("friday"));//查找近10次的数据
                    // List<AdBuildDeployReport> adBuildDeployReportList = adBuildDeployReportPagedList.getList();
                    int ants = adBuildDeployReportPagedList.size();
                    if (ants == 0) {
                        weekDate++;//判断某个project是否没有数据
                        mapDate = dateMap.get("friday");
                    } else {
                        mapDate = yarformat.format(adBuildDeployReportPagedList.get(0).getDate("end_date"));
                    }
                    log.error("mapDate======" + mapDate);
                    for (int antNum = 10 - ants; antNum >= 1; antNum--) {
                        BuildDeployTimesValueExtPojo bulidTimes = new BuildDeployTimesValueExtPojo();
                        BuildDeployTimesValueExtPojo deployTimes = new BuildDeployTimesValueExtPojo();
                        BuildDeployDateExtPojo buildDeployDateExtPojo = new BuildDeployDateExtPojo();
                        begin_date = format.format(adSonar.getEveryDate(mapDate, -antNum * 7));

                        log.error("ca==antNum" + antNum + "====" + begin_date);
                        end_date = format.format(adSonar.getEveryDate(mapDate, -antNum * 7 - 4));
                        log.error("ca==antNum" + antNum + "====" + end_date);
                        if (count == 1) {
                            buildDeployDateExtPojo.setDate(end_date + "-" + begin_date);
                            buildDeployDateExtPojoList.add(buildDeployDateExtPojo);
                        }
                        bulidTimes.setTimes(-1d);
                        buildTimesList.add(bulidTimes);
                        deployTimes.setTimes(-1d);
                        deployTimesList.add(deployTimes);
                    }
                    for (int j = 0; j < ants; j++) {
                        // System.out.println("j=========================" + j);
                        BuildDeployTimesValueExtPojo bulidTimes = new BuildDeployTimesValueExtPojo();
                        BuildDeployTimesValueExtPojo deployTimes = new BuildDeployTimesValueExtPojo();
                        BuildDeployDateExtPojo buildDeployDateExtPojo = new BuildDeployDateExtPojo();
                        if (count == 1) {
                            buildDeployDateExtPojo.setDate(format.format(adBuildDeployReportPagedList.get(j).getDate("begin_date")) + "-" + format.format(adBuildDeployReportPagedList.get(j).getDate("end_date")));
                            buildDeployDateExtPojoList.add(buildDeployDateExtPojo);
                        }
                        bulidTimes.setTimes(Double.parseDouble(df.format(adBuildDeployReportPagedList.get(j).getDouble("b_average_times"))));
                        buildTimesList.add(bulidTimes);
                        deployTimes.setTimes(Double.parseDouble(df.format(adBuildDeployReportPagedList.get(j).getDouble("d_average_times"))));
                        deployTimesList.add(deployTimes);
                    }

                    buildDeployTimesExtPojo.setProjectName(builderType.get(jobName[i]));
                    buildDeployTimesExtPojo.setBuild(buildTimesList);
                    buildDeployTimesExtPojo.setDeploy(deployTimesList);
                    if (ants < 2) {
                        buildDeployTimesExtPojo.setBuildTimes(0d);
                        buildDeployTimesExtPojo.setDeployTimes(0d);
                    } else {
                        System.out.println("======++++++++" + adBuildDeployReportPagedList.get(ants - 1).getDouble("b_average_times") + "==" + adBuildDeployReportPagedList.get(ants - 2).getDouble("b_average_times"));
                        if (adBuildDeployReportPagedList.get(ants - 1).getDouble("b_average_times") == 0) {
                            buildDeployTimesExtPojo.setBuildTimes(0d);
                        } else {
                            buildDeployTimesExtPojo.setBuildTimes(Double.parseDouble(df.format(((adBuildDeployReportPagedList.get(ants - 1).getDouble("b_average_times") - adBuildDeployReportPagedList.get(ants - 2).getDouble("b_average_times")) / adBuildDeployReportPagedList.get(ants - 1).getDouble("b_average_times")) * 100)));
                        }
                        if (adBuildDeployReportPagedList.get(ants - 1).getDouble("d_average_times") == 0) {
                            buildDeployTimesExtPojo.setDeployTimes(0d);
                        } else {
                            buildDeployTimesExtPojo.setDeployTimes(Double.parseDouble(df.format(((adBuildDeployReportPagedList.get(ants - 1).getDouble("d_average_times") - adBuildDeployReportPagedList.get(ants - 2).getDouble("d_average_times")) / adBuildDeployReportPagedList.get(ants - 1).getDouble("d_average_times")) * 100)));
                        }
                    }
                    buildDeployTimesExtPojoList.add(buildDeployTimesExtPojo);
                }
                if (weekDate == jobName.length) {
                    return null;
                }
                adBuildDeployTimesPojo.setDates(buildDeployDateExtPojoList);
                adBuildDeployTimesPojo.setReports(buildDeployTimesExtPojoList);
                return JsonpUtil.modelToJson(adBuildDeployTimesPojo);
            } else {
                for (int i = 0; i < jobName.length; i++) {
                    count++;
                    List<BuildDeployTimesValueExtPojo> buildTimesList = new ArrayList<>();
                    List<BuildDeployTimesValueExtPojo> deployTimesList = new ArrayList<>();
                    BuildDeployTimesExtPojo buildDeployTimesExtPojo = new BuildDeployTimesExtPojo();
                    List<SqlRow> sqlRowList = adBuildDeployReport.getMonthyDataByDate(jobName[i], (String) map.get("date"));//TODO 获取最近一条月报
                    List<SqlRow> beforList = adBuildDeployReport.getMonthyDataByDate(jobName[i], firstDay);//TODO 获取上个月的月报
                    List<SqlRow> list = adBuildDeployReport.getMonthData(jobName[i], mdate);//TODO 获取这个月每天的信息
                    int listCount = list.size();
                    for (int day = 1; day <= lastDay; day++) {
                        BuildDeployTimesValueExtPojo bulidTimes = new BuildDeployTimesValueExtPojo();
                        BuildDeployTimesValueExtPojo deployTimes = new BuildDeployTimesValueExtPojo();
                        BuildDeployDateExtPojo buildDeployDateExtPojo = new BuildDeployDateExtPojo();
                        if (count == 1) {
                            buildDeployDateExtPojo.setDate(mdate.split("-")[1] + "." + day);
                            buildDeployDateExtPojoList.add(buildDeployDateExtPojo);
                        }
                        if (sqlRowList.size() != 0 && listCount != 0) {
                            sqlDay = Integer.parseInt(format.format(list.get(listCount - 1).getDate("create_time")).split("\\.")[1]);
                            //log.error("day=====================" + day + "sqlDay====================" + sqlDay);
                            if (day == sqlDay) {
                                log.error("当月的构建部署时长数据不为0:222222222=================================================" + day);
                                bulidTimes.setTimes(Double.parseDouble(df.format(list.get(listCount - 1).getDouble("b_execu_times"))));
                                buildTimesList.add(bulidTimes);
                                deployTimes.setTimes(Double.parseDouble(df.format(list.get(listCount - 1).getDouble("d_execu_times"))));
                                deployTimesList.add(deployTimes);
                                listCount--;
                                continue;
                            }
                        }
                        log.error("当月的构建部署时长数据333333333333=================================================" + day);
                        bulidTimes.setTimes(-1d);
                        buildTimesList.add(bulidTimes);
                        deployTimes.setTimes(-1d);
                        deployTimesList.add(deployTimes);
                    }
                    buildDeployTimesExtPojo.setProjectName(builderType.get(jobName[i]));
                    buildDeployTimesExtPojo.setBuild(buildTimesList);
                    buildDeployTimesExtPojo.setDeploy(deployTimesList);
                    if (sqlRowList.size() == 0) {
                        buildDeployTimesExtPojo.setBuildTimes(0d);
                        buildDeployTimesExtPojo.setDeployTimes(0d);
                        buildDeployTimesExtPojo.setBuildTimesHis(0d);
                        buildDeployTimesExtPojo.setDeployTimesHis(0d);
                        monthDate++;
                    } else if (sqlRowList.get(0).getDouble("b_average_times") != 0) {
                        buildDeployTimesExtPojo.setBuildTimes(Double.parseDouble(df.format(sqlRowList.get(0).getDouble("b_average_times"))));
                        buildDeployTimesExtPojo.setDeployTimes(Double.parseDouble(df.format(sqlRowList.get(0).getDouble("d_average_times"))));
                        if (beforList.size() != 0) {
                            buildDeployTimesExtPojo.setBuildTimesHis(Double.parseDouble(df.format(((sqlRowList.get(0).getDouble("b_average_times") - beforList.get(0).getDouble("b_average_times")) / sqlRowList.get(0).getDouble("b_average_times")) * 100)));
                            if (sqlRowList.get(0).getDouble("d_average_times") != 0) {
                                buildDeployTimesExtPojo.setDeployTimesHis(Double.parseDouble(df.format(((sqlRowList.get(0).getDouble("d_average_times") - beforList.get(0).getDouble("d_average_times")) / sqlRowList.get(0).getDouble("d_average_times")) * 100)));
                            } else {
                                buildDeployTimesExtPojo.setDeployTimesHis(0d);
                            }
                        } else {
                            buildDeployTimesExtPojo.setBuildTimesHis(100d);
                            buildDeployTimesExtPojo.setDeployTimesHis(100d);
                        }
                    } else {
                        buildDeployTimesExtPojo.setBuildTimes(Double.parseDouble(df.format(sqlRowList.get(0).getDouble("b_average_times"))));
                        buildDeployTimesExtPojo.setDeployTimes(Double.parseDouble(df.format(sqlRowList.get(0).getDouble("d_average_times"))));
                        buildDeployTimesExtPojo.setBuildTimesHis(0d);
                        if (beforList.size() != 0) {
                            if (sqlRowList.get(0).getDouble("d_average_times") != 0) {
                                buildDeployTimesExtPojo.setDeployTimesHis(Double.parseDouble(df.format(((sqlRowList.get(0).getDouble("d_average_times") - beforList.get(0).getDouble("d_average_times")) / sqlRowList.get(0).getDouble("d_average_times")) * 100)));
                            } else {
                                buildDeployTimesExtPojo.setDeployTimesHis(0d);
                            }
                        } else {
                            buildDeployTimesExtPojo.setDeployTimesHis(100d);
                        }
                    }
                    buildDeployTimesExtPojoList.add(buildDeployTimesExtPojo);

                }

                adBuildDeployTimesPojo.setDates(buildDeployDateExtPojoList);
                adBuildDeployTimesPojo.setReports(buildDeployTimesExtPojoList);
                if (monthDate == jobName.length) {
                    return null;
                }
                return JsonpUtil.modelToJson(adBuildDeployTimesPojo);
            }
        } else {
            return null;
        }
    }

    public void setAdSonarReport(SonarReportExtPojo reportExtPojo, String name, String date, List<SonarReportExtPojo> sonarReportExtPojoList) {
        reportExtPojo.setEnvName(name);
        reportExtPojo.setDate(date);
        reportExtPojo.setFileNum(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setBlockPro(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setCodeCover(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setCodeLine(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setMethodComp(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setRepeatRate(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setMethodNum(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setSeriousPro(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setTotalComp(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setUnitNum(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setUnitSuc(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setBlockPro(new SonarValueExtPojo(-1, 0));
        reportExtPojo.setUnitTime(new SonarValueExtPojo(-1, 0));
        sonarReportExtPojoList.add(reportExtPojo);
    }

    @RequestMapping(value = "/qrySonarDateReport", produces = "application/json")
    public String qrySonarDateReport(HttpServletRequest request, @RequestParam Map<String, String> params) {
        Long userId = getUserId(request);
        String startDate = params.get("startDate");
        String filters = params.get("filters");
        String groupIds = params.get("groupIds");
        if (groupIds == null || groupIds.equals("")) {
            groupIds = "0";
        }
        userDataRelateImpl.updateRel(userId, 1, 1, filters);
        userDataRelateImpl.updateRel(userId, 2, 1, groupIds);
        AdEasyUIPojo poj;
        if (params.containsKey("endDate") && !params.get("endDate").equals(startDate)) {
            poj = adSonarReport.qrySonarDateReport(groupIds, filters, startDate, params.get("endDate"));
        } else {
            poj = adSonarReport.qrySonarDateReport(groupIds, filters, startDate, null);
        }
        return JsonpUtil.modelToJson(poj);
    }

    @RequestMapping(value = "/sonarOutputExcel")
    public WritableWorkbook OutputExcel(HttpServletResponse response, HttpServletRequest request, @RequestParam Map<String, String> params) throws IOException {
        String startDate = params.get("startDate");
        String filters = params.get("filters");
        String groupIds = params.get("groupIds");
        String fileName = "Sonar质量报表";
        if (groupIds == null || groupIds.equals("")) {
            groupIds = "0";
        }
        List<ExcelCellHeadExtPojo> headList = new ArrayList<>();
        List<ExcelCellHeadExtPojo> headExtList = new ArrayList<>();
        List<ExcelCellExtPojo> contents = new ArrayList<>();
        adSonarReport.setSonarReportHead(headList, headExtList, filters);
        if (params.containsKey("endDate") && !params.get("endDate").equals("") && !params.get("endDate").equals(startDate)) {
            fileName += "_" + startDate + "_VS_" + params.get("endDate");
            adSonarReport.setSonarReportContent(contents, groupIds, filters, startDate, params.get("endDate"));
        } else {
            fileName += "_" + startDate;
            adSonarReport.setSonarReportContent(contents, groupIds, filters, startDate, null);
        }
        return excelOutputService.createExcelOutputExcel(response, request, headList, contents, fileName, headExtList);
    }

}
