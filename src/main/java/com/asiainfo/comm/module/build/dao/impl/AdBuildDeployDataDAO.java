package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.comm.module.build.service.impl.AdGroupImpl;
import com.asiainfo.comm.module.models.AdBuildDeployData;
import com.asiainfo.comm.module.models.AdBuildDeployReport;
import com.asiainfo.comm.module.models.query.QAdBuildDeployData;
import com.asiainfo.comm.module.models.query.QAdBuildDeployReport;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by zhangpeng on 2016/7/12.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class AdBuildDeployDataDAO {
    @Autowired
    AdBuildDeployReportDAO adBuildDeployReportDAO;
    @Autowired
    AdGroupImpl adGroupImpl;

    public long getBuildLogData(long project_id) {//TODO 获取当天的构建信息
        String sql = "select    /*+parallel(c,32)*/ n.project_name,\n" +
            "       n.project_id,n.branch_name, " +
            "       c.total_step,\n" +
            "       nvl(sum(c.finish_date - c.begin_date) * 24 * 3600,0) times,\n" +
            "       sum(c.stage_result) results\n" +
            "  from (select m.project_name,\n" +
            "               m.project_id,\n" +
            "               m.branch_id,\n" +
            "               m.branch_name,\n" +
            "               b.stage_id\n" +
            "          from (select t.project_name,\n" +
            "                       t.project_id,\n" +
            "                       a.branch_id,\n" +
            "                       a.branch_name\n" +
            "                  from ad_project t, ad_branch a\n" +
            "                 where t.project_id = :project_id\n" +
            "                   and t.project_id = a.project_id\n" +
            "                   and a.state=1) m,\n" +
            "               ad_stage b\n" +
            "         where m.branch_id = b.branch_id and b.state=1\n" +
            "           and  b.stage_code in(1,2,5)) n,\n" +
            "       ad_stage_log_dtl c\n" +
            " where c.stage_result in (2,3)\n" +
            "   and to_char(c.begin_date, 'yyyy-MM-dd') >\n" +
            "       to_char(sysdate - 1, 'yyyy-MM-dd')\n" +
            "   and c.stage_id = n.stage_id\n" +
            " group by n.project_name,n.project_id, n.branch_name,c.total_step\n";
        SqlQuery sqlQuery = createSqlQuery(sql);
        sqlQuery.setParameter("project_id", project_id);
        List<SqlRow> list = sqlQuery.findList();
        int count = list.size();
        double times = 0;
        double success = 0;
        double fsuccess = 0;//记录只有下载节点的构建情况
        AdBuildDeployData adBuildDeployData = new AdBuildDeployData();
        for (SqlRow sqlRow : list) {
            if (sqlRow.getDouble("results") == 4) {
                success++;
                times += sqlRow.getDouble("times");
            }
            if (sqlRow.getDouble("results") == 2) {
                fsuccess++;
            }
        }
        deleBuildByprojectId(project_id);
        if (count > 0) {
            DecimalFormat df = new DecimalFormat("#.0");
            if (success > 0) {
                adBuildDeployData.setBExecuTimes(Double.parseDouble(df.format(times / success)));
            } else {
                adBuildDeployData.setBExecuTimes(0d);
            }
            adBuildDeployData.setBSuccessNum(success);
            adBuildDeployData.setBFailNum(count - success - fsuccess);
            adBuildDeployData.setCreateTime(new Date());
            adBuildDeployData.setDSuccessNum(0d);
            adBuildDeployData.setDFailNum(0d);
            adBuildDeployData.setDExecuTimes(0d);
            adBuildDeployData.setProjectId(project_id);
            adBuildDeployData.save();
        } else {
            adBuildDeployData.setProjectId(project_id);
            adBuildDeployData.setBExecuTimes(0d);
            adBuildDeployData.setBSuccessNum(0d);
            adBuildDeployData.setBFailNum(0d);
            adBuildDeployData.setCreateTime(new Date());
            adBuildDeployData.setDSuccessNum(0d);
            adBuildDeployData.setDFailNum(0d);
            adBuildDeployData.setDExecuTimes(0d);
            adBuildDeployData.save();
            log.error("没有查到" + new Date() + "当天的构建数据");
        }
        return adBuildDeployData.getId();
    }

    public void getBuildLogDeployData(long project_id) {//TODO 获取当天的部署信息
        long build_id = getBuildLogData(project_id);//先获取当天的构建信息
        String sql = "select t.log_id,t.deploy_result,(t.end_time - t.start_time) * 24 * 3600 as times\n" +
            "  from ad_systemdeploy_log t\n" +
            " where t.project_id = :project_id\n" +
            "   and to_char(t.start_time, 'yyyy-MM-dd') >\n" +
            "       to_char(sysdate - 1, 'yyyy-MM-dd')\n" +
            "   and t.end_time is not null";
        SqlQuery sqlQuery1 = createSqlQuery(sql);
        sqlQuery1.setParameter("project_id", project_id);
        List<SqlRow> list = sqlQuery1.findList();
        int count = list.size();
        double deploy_times = 0;
        double deploy_success = 0;
        System.out.println("部署执行了：" + count + "次");
        for (SqlRow sqlRow : list) {
            if (sqlRow.getDouble("deploy_result") == 1) {
                deploy_success++;
                deploy_times += sqlRow.getDouble("times");
            }
        }
        if (count != 0) {
            DecimalFormat df = new DecimalFormat("#.0");
            AdBuildDeployData adBuildDeployData = getListById(build_id).get(0);
            adBuildDeployData.setDSuccessNum(deploy_success);
            adBuildDeployData.setDFailNum(count - deploy_success);
            if (deploy_success > 0) {
                adBuildDeployData.setDExecuTimes(Double.parseDouble(df.format(deploy_times / deploy_success)));
            } else {
                adBuildDeployData.setDExecuTimes(0d);
            }
            adBuildDeployData.update();
        } else {
            log.error("没有查到" + new Date() + "当天的部署数据");
        }

    }

    public List<AdBuildDeployData> getListById(Long id) {
        List<AdBuildDeployData> adBuildDeployDatas = new QAdBuildDeployData().id.eq(id).findList();
        return adBuildDeployDatas;
    }

    public void deleBuildByprojectId(long project_id) {//TODO 根据projectId删除最近一条数据
        String sql = "delete ad_build_deploy_data t where t.project_id = :project_id and to_char(t.create_time,'yyyy-MM-dd')>to_char(sysdate-1,'yyyy-MM-dd')";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("project_id", project_id);
        Ebean.execute(update);
    }

    public void getBuildByName(String project_name) {//TODO 计算周报
        String sql = "select * from ad_build_deploy_data t where t.project_name = :project_name and to_char(t.create_time,'yyyy-MM-dd') between  to_char(sysdate-4,'yyyy-MM-dd') and to_char(sysdate,'yyyy-MM-dd')";
        SqlQuery sqlQuery1 = createSqlQuery(sql);
        sqlQuery1.setParameter("project_name", project_name);
        List<SqlRow> list = sqlQuery1.findList();
        int bcount = list.size();//TODO 计算某天没有构建次数的
        int dcount = list.size();
        String weekBranchName = "";
        double bTimes = 0;
        double dTimes = 0;
        double bSuccess = 0;
        double dSuccess = 0;
        double bFail = 0;
        double dFail = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -4);  //设置为前4天
        Date dBefore = calendar.getTime();
        AdBuildDeployReport adBuildDeployReport = new AdBuildDeployReport();
        for (SqlRow sqlRow : list) {
            weekBranchName = sqlRow.getString("brach_name");
            bTimes += sqlRow.getDouble("b_execu_times");
            dTimes += sqlRow.getDouble("d_execu_times");
            bSuccess += sqlRow.getDouble("b_success_num");
            dSuccess += sqlRow.getDouble("d_success_num");
            bFail += sqlRow.getDouble("b_fail_num");
            dFail += sqlRow.getDouble("d_fail_num");
            if (sqlRow.getDouble("b_execu_times") == 0) {
                bcount -= 1;
            }
            if (sqlRow.getDouble("d_execu_times") == 0) {
                dcount -= 1;
            }
        }
        adBuildDeployReportDAO.deleBuildReportDataByName(project_name, 1d);
        if (bcount != 0) {
            DecimalFormat df = new DecimalFormat("#.00");
            DecimalFormat rdf = new DecimalFormat("#.000");
            adBuildDeployReport.setProjectName(project_name);
            adBuildDeployReport.setBranchName(weekBranchName);
            adBuildDeployReport.setBAverageTimes(Double.parseDouble(df.format(bTimes / bcount)));
            if (dcount != 0) {
                adBuildDeployReport.setDAverageTimes(Double.parseDouble(df.format(dTimes / dcount)));
            } else {
                adBuildDeployReport.setDAverageTimes(0d);
            }
            adBuildDeployReport.setType(1d);//周报
            adBuildDeployReport.setBeginDate(dBefore);
            adBuildDeployReport.setEndDate(new Date());
            adBuildDeployReport.setBSuccessRate(Double.parseDouble(rdf.format(bSuccess / (bSuccess + bFail))) * 100);
            adBuildDeployReport.setDSuccessRate(Double.parseDouble(rdf.format(dSuccess / (dSuccess + dFail))) * 100);
            adBuildDeployReport.save();
            log.error("插入周报成功！！！！！");
        } else {
            adBuildDeployReport.setProjectName(project_name);
            adBuildDeployReport.setBranchName(weekBranchName);
            adBuildDeployReport.setBAverageTimes(0d);
            adBuildDeployReport.setDAverageTimes(0d);
            adBuildDeployReport.setType(1d);//周报
            adBuildDeployReport.setBeginDate(dBefore);
            adBuildDeployReport.setEndDate(new Date());
            adBuildDeployReport.setBSuccessRate(0d);
            adBuildDeployReport.setDSuccessRate(0d);
            adBuildDeployReport.save();
            log.error("各个日报数据为0，插入周报数据为0！！！！！");
        }
        setBuildDeployDateExt1(list, adBuildDeployReport.getId());//TODO 反向往日报里写周报的编号在ext1中
    }

    public List<AdBuildDeployReport> getWeekReportData(String name) {
        List<AdBuildDeployReport> adBuildDeployReports = new QAdBuildDeployReport().projectName.eq(name).type.eq(1).orderBy("end_date desc").findList();
        return adBuildDeployReports;
    }

    public void setBuildDeployDateExt1(List<SqlRow> list, long id) {
        int num = list.size();
        while (num > 0) {
            num--;
            System.out.println("num=" + num);
            AdBuildDeployData adBuildDeployData = new QAdBuildDeployData().id.eq(list.get(num).getInteger("id")).findList().get(0);
            adBuildDeployData.setExt1(id);
            adBuildDeployData.update();
        }
    }

    public List<SqlRow> getWeekReportDataByDate(String project_name, Long type, String date) {//TODO 根据日期获取构建部署相应的周报
        String sql = "select t.* from ad_build_deploy_report t where t.type= :type and t.project_name= :project_name and \n" +
            ":date between to_char(t.begin_date,'yyyy-MM-dd') and  to_char(t.end_date,'yyyy-MM-dd')";
        SqlQuery sqlQuery1 = createSqlQuery(sql);
        sqlQuery1.setParameter("project_name", project_name);
        sqlQuery1.setParameter("type", type);
        sqlQuery1.setParameter("date", date);
        List<SqlRow> list = sqlQuery1.findList();
        return list;
    }

    public List<AdBuildDeployReport> getBuildDeployDateById(Long id) {
        List<AdBuildDeployReport> adBuildDeploy = new QAdBuildDeployReport().id.eq(id).findList();
        return adBuildDeploy;
    }

    public List<SqlRow> getNearlyTenBuildLog(long projectId, long branchId) {// TODO 获取近10次的构建情况
        String sql = "select p.project_name,p.branch_name,p.total_step,p.times,p.results,p.start_date from (select /*+parallel(c,32)*/ n.project_name,\n" +
            "                   n.branch_name,\n" +
            "                   c.total_step,\n" +
            "                   nvl(sum(c.finish_date - c.begin_date) * 24 * 3600,0) times,\n" +
            "                   sum(c.stage_result) results,\n" +
            "                   min(c.begin_date) start_date\n" +
            "              from (select m.project_name,\n" +
            "                           m.project_id,\n" +
            "                           m.branch_id,\n" +
            "                           m.branch_name,\n" +
            "                           b.stage_id\n" +
            "                      from (select t.project_name,\n" +
            "                                   t.project_id,\n" +
            "                                   a.branch_id,\n" +
            "                                   a.branch_name\n" +
            "                              from ad_project t, ad_branch a\n" +
            "                             where t.project_id= :projectId" +
            "                               and t.project_id = a.project_id\n" +
            "                                      and  a.branch_id=:branchId" +
            "                               and a.state=1) m,\n" +
            "                           ad_stage b\n" +
            "                     where m.branch_id = b.branch_id and b.state=1\n" +
            "                       and  b.stage_code in(1,2,5)) n,\n" +
            "                   ad_stage_log_dtl c\n" +
            "             where c.stage_result in (2,3)\n" +
            "               and c.stage_id = n.stage_id\n" +
            "             group by n.project_name, n.branch_name,c.total_step \n" +
            "             order by start_date desc ) p where rownum<=10 ";
        SqlQuery sqlQuery1 = createSqlQuery(sql);
        sqlQuery1.setParameter("projectId", projectId);
        sqlQuery1.setParameter("branchId", branchId);
        List<SqlRow> list = sqlQuery1.findList();
        return list;
    }

    /**
     * 修改防止sql注入
     *
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @param groupIds  groupId
     * @param qryType   查询类型
     * @return 查询结果
     */
    public List<Map<String, String>> qryBuildLogReport(String beginDate, String endDate, String groupIds, int qryType) {
        String sql;
        String[] splitIds = StringUtils.split(groupIds, ",");
        List<Long> ids = new ArrayList<>();
        for (String id : splitIds) {
            ids.add(Long.parseLong(id));
        }
        SimpleDateFormat sTime = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat eTime = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss");
        Date startDate = null;
        Date endTime = null;
        try {
            startDate = sTime.parse(beginDate);
            endTime = eTime.parse(endDate + " 23:59:59");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        sql = "SELECT nvl(sum(a.b_success_num),0) b_success_num,nvl(sum(a.b_fail_num),0) b_fail_num,nvl(sum(a.b_execu_times),0) b_execu_times,nvl(sum(a.d_success_num),0) d_success_num,nvl(sum(a.d_fail_num),0) d_fail_num,nvl(sum(a.d_execu_times),0) d_execu_times,d.group_name,d.group_id  FROM  ad_build_deploy_data a," +
            "(select c.project_id,b.group_name,b.group_id from ad_group b,ad_project c where b.group_id=c.group_id and b.state=1 and b.group_id in(:groupIds)) d WHERE a.project_id(+)=d.project_id and a.create_time(+)> :beginDate and a.create_time(+)< :endDate group by d.group_name,d.group_id order by d.group_id";
        SqlQuery sqlQuery = createSqlQuery(sql).setParameter("groupIds", ids).setParameter("beginDate", startDate).setParameter("endDate", endTime);
        List<SqlRow> list = sqlQuery.findList();
        Map<String, String> buildLogMap;
        Map<String, Map<String, String>> retbuildLogMap = new HashMap<>();
        for (SqlRow sqlRow : list) {
            buildLogMap = new HashMap<>();
            buildLogMap.put("b_success_num", "" + sqlRow.get("b_success_num"));
            buildLogMap.put("b_fail_num", "" + sqlRow.get("b_fail_num"));
            buildLogMap.put("b_execu_times", "" + sqlRow.get("b_execu_times"));
            buildLogMap.put("d_success_num", "" + sqlRow.get("d_success_num"));
            buildLogMap.put("d_fail_num", "" + sqlRow.get("d_fail_num"));
            buildLogMap.put("d_execu_times", "" + sqlRow.get("d_execu_times"));
            buildLogMap.put("group_name", "" + sqlRow.get("group_name"));
            buildLogMap.put("group_id", "" + sqlRow.get("group_id"));
            retbuildLogMap.put("" + buildLogMap.get("group_id"), buildLogMap);
        }
        String newSql;
        StringBuilder sb = new StringBuilder();
        String[] groups = groupIds.split(",");
        for (String groupId : groups) {
            sb.append("nvl(sum(decode(t.group_id,").append(groupId).append(",t.b_success_num,null)),0) bsucnum").append(groupId).append(",");
            sb.append("nvl(sum(decode(t.group_id,").append(groupId).append(",t.b_fail_num,null)),0) bfailnum").append(groupId).append(",");
            sb.append("nvl(sum(decode(t.group_id,").append(groupId).append(",t.d_success_num,null)),0) dsucnum").append(groupId).append(",");
            sb.append("nvl(sum(decode(t.group_id,").append(groupId).append(",t.d_fail_num,null)),0) dfailnum").append(groupId).append(",");
        }
        newSql = sb.toString();
        if (sql.length() > 1) {
            newSql = newSql.substring(0, newSql.length() - 1);
        }
        sql = " select t.create_time,:newSql from (" +
            " SELECT nvl(sum(a.b_success_num),0) b_success_num,nvl(sum(a.b_fail_num),0) b_fail_num,nvl(sum(a.d_success_num),0) d_success_num,nvl(sum(a.d_fail_num),0) d_fail_num,d.group_name,d.group_id,trunc(a.create_time) create_time  FROM  ad_build_deploy_data a,(select c.project_id,b.group_name,b.group_id from ad_group b,ad_project c where b.group_id=c.group_id and b.state=1 and b.group_id in(:groupIds)) d WHERE a.project_id(+)=d.project_id and a.create_time> :beginDate and a.create_time< :endDate group by d.group_name,d.group_id,trunc(a.create_time) order by d.group_id) t group by t.create_time order by t.create_time";
        SqlQuery sqlQuery2 = createSqlQuery(sql).setParameter("newSql", newSql).setParameter("groupIds", ids).setParameter("beginDate", startDate).setParameter("endDate", endTime);
        List<SqlRow> list2 = sqlQuery2.findList();
        Map<String, String> buildlogDtlMap;
        Map<String, String> deploylogDtlMap;
        List<Map<String, String>> buildlogDtlList;
        List<Map<String, String>> deploylogDtlList;
        Map<String, List<Map<String, String>>> retbuildlogDtlMap = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> retdeploylogDtlMap = new LinkedHashMap<>();
        String createtime;
        for (String groupId : groups) {
            buildlogDtlList = new ArrayList<>();
            deploylogDtlList = new ArrayList<>();
            for (SqlRow sqlRow : list2) {
                buildlogDtlMap = new HashMap<>();
                deploylogDtlMap = new HashMap<>();
                createtime = sqlRow.getString("create_time");
                createtime = createtime.replaceAll("\\/", "-");
                if (createtime.contains("-") && createtime.contains(" ")) {
                    createtime = createtime.substring(createtime.indexOf("-") + 1, createtime.indexOf(" "));
                }
                buildlogDtlMap.put(createtime + "-true", "" + sqlRow.get("bsucnum" + groupId));
                buildlogDtlMap.put(createtime + "-false", "" + sqlRow.get("bfailnum" + groupId));
                deploylogDtlMap.put(createtime + "-true", "" + sqlRow.get("dsucnum" + groupId));
                deploylogDtlMap.put(createtime + "-false", "" + sqlRow.get("dfailnum" + groupId));
                buildlogDtlList.add(buildlogDtlMap);
                deploylogDtlList.add(deploylogDtlMap);
            }
            retbuildlogDtlMap.put("" + groupId, buildlogDtlList);
            retdeploylogDtlMap.put("" + groupId, deploylogDtlList);
        }
        Map<String, String> retMap;
        List<Map<String, String>> retList = new ArrayList<>();
        long ll_success;
        long ll_false;
        Map<String, String> buildMap;
        boolean buildBool = false;
        boolean deployBool = false;
        switch (qryType) {
            case 0:
                buildBool = true;
                deployBool = true;
                break;
            case 1:
                buildBool = true;
                break;
            case 2:
                deployBool = true;
                break;
        }
        //根据项目排序
        for (String groupId : groups) {
            //构建
            if (buildBool && retbuildLogMap.containsKey("" + groupId)) {
                retMap = new LinkedHashMap<>();
                buildMap = retbuildLogMap.get("" + groupId);
                ll_success = 0;
                ll_false = 0;
                if (buildMap != null) {
                    retMap.put("productName", buildMap.get("group_name"));
                    retMap.put("link", "构建");
                    retMap.put("success", buildMap.get("b_success_num"));
                    retMap.put("false", buildMap.get("b_fail_num"));
                    if (StringUtils.isNotEmpty(retMap.get("success"))) {
                        ll_success = Long.valueOf(retMap.get("success"));
                    }
                    if (StringUtils.isNotEmpty(retMap.get("false"))) {
                        ll_false = Long.valueOf(retMap.get("false"));
                    }
                    retMap.put("allCount", "" + (ll_success + ll_false));
                    if ((ll_success + ll_false) != 0) {
                        retMap.put("proSuccess", "" + (ll_success * 100 / (ll_success + ll_false)) + "%");
                    } else {
                        retMap.put("proSuccess", "0");
                    }
                    retMap.put("duration", buildMap.get("b_execu_times"));
                } else {
                    retMap.put("productName", adGroupImpl.qryById(Long.valueOf(groupId)).getGroupName());
                    retMap.put("link", "构建");
                    retMap.put("success", "0");
                    retMap.put("false", "0");
                    retMap.put("allCount", "0");
                    retMap.put("proSuccess", "0");
                    retMap.put("duration", "0");
                }
                List<String> dateList = DateConvertUtils.getDates(beginDate, endDate);
                if (dateList != null) {
                    for (String d1 : dateList) {
                        if (!retMap.containsKey(d1 + "-true")) {
                            retMap.put(d1 + "-true", "0");
                            retMap.put(d1 + "-false", "0");
                        }
                    }

                }
                int jj = 0;
                if (retbuildlogDtlMap.containsKey("" + groupId)) {
                    List<Map<String, String>> rebuildList = retbuildlogDtlMap.get("" + groupId);
                    if (rebuildList != null) {
                        for (Map<String, String> hmap : rebuildList) {
                            for (Map.Entry<String, String> entry : retMap.entrySet()) {
                                if (hmap.containsKey(entry.getKey())) {
                                    retMap.put(entry.getKey(), hmap.get(entry.getKey()));
                                    if (!("0").equals(hmap.get(entry.getKey())) && entry.getKey().indexOf("true") > 0) {
                                        jj++;
                                    }
                                }
                            }
                        }
                    }
                }
                if (jj > 0 && (retMap.get("duration") != null && StringUtils.isNotEmpty(retMap.get("duration")))) {
                    BigDecimal b = new BigDecimal(Float.valueOf(retMap.get("duration")) / jj);
                    float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                    retMap.put("duration", "" + f1);
                }
                retList.add(retMap);
            }
            //部署
            if (deployBool && retbuildLogMap.containsKey("" + groupId)) {
                retMap = new LinkedHashMap<>();
                buildMap = retbuildLogMap.get("" + groupId);
                ll_success = 0;
                ll_false = 0;
                if (buildMap != null) {
                    retMap = new LinkedHashMap<>();
                    retMap.put("productName", buildMap.get("group_name"));
                    retMap.put("link", "发布");
                    retMap.put("success", buildMap.get("d_success_num"));
                    retMap.put("false", buildMap.get("d_fail_num"));
                    if (StringUtils.isNotEmpty(retMap.get("success"))) {
                        ll_success = Long.valueOf(retMap.get("success"));
                    }
                    if (StringUtils.isNotEmpty(retMap.get("false"))) {
                        ll_false = Long.valueOf(retMap.get("false"));
                    }
                    retMap.put("allCount", "" + (ll_success + ll_false));
                    if ((ll_success + ll_false) != 0) {
                        retMap.put("proSuccess", "" + (ll_success * 100 / (ll_success + ll_false)) + "%");
                    } else {
                        retMap.put("proSuccess", "0");
                    }
                    retMap.put("duration", buildMap.get("d_execu_times"));
                } else {
                    retMap.put("productName", adGroupImpl.qryById(Long.valueOf(groupId)).getGroupName());
                    retMap.put("link", "发布");
                    retMap.put("success", "0");
                    retMap.put("false", "0");
                    retMap.put("allCount", "0");
                    retMap.put("proSuccess", "0");
                    retMap.put("duration", "0");
                }
                List<String> dateList = DateConvertUtils.getDates(beginDate, endDate);
                if (dateList != null) {
                    for (String d1 : dateList) {
                        if (!retMap.containsKey(d1 + "-true")) {
                            retMap.put(d1 + "-true", "0");
                            retMap.put(d1 + "-false", "0");
                        }
                    }
                }
                int kk = 0;
                if (retdeploylogDtlMap.containsKey("" + groupId)) {
                    List<Map<String, String>> redeployList = retdeploylogDtlMap.get("" + groupId);
                    if (redeployList != null) {
                        for (Map<String, String> hmap : redeployList) {
                            for (Map.Entry<String, String> entry : retMap.entrySet()) {
                                if (hmap.containsKey(entry.getKey())) {
                                    retMap.put(entry.getKey(), hmap.get(entry.getKey()));
                                    if (!("0").equals(hmap.get(entry.getKey())) && entry.getKey().indexOf("true") > 0) {
                                        kk++;
                                    }
                                }
                            }
                        }
                    }
                }
                if (kk > 0 && (retMap.get("duration") != null && StringUtils.isNotEmpty(retMap.get("duration")))) {
                    BigDecimal b = new BigDecimal(Float.valueOf(retMap.get("duration")) / kk);
                    float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                    retMap.put("duration", "" + f1);
                }
                retList.add(retMap);
            }
        }
        return retList;
    }

    //新报表管理接口
    public List<Map<String, Object>> qryBuildLogReportV2(String beginDate, String endDate, String groupIds, int qryType) {
        String sql;
        String[] splitIds = StringUtils.split(groupIds, ",");
        List<Long> ids = new ArrayList<>();
        for (String id : splitIds) {
            ids.add(Long.parseLong(id));
        }
        sql = "SELECT nvl(sum(a.b_success_num),0) b_success_num,nvl(sum(a.b_fail_num),0) b_fail_num,nvl(sum(a.b_execu_times),0) b_execu_times,nvl(sum(a.d_success_num),0) d_success_num,nvl(sum(a.d_fail_num),0) d_fail_num,nvl(sum(a.d_execu_times),0) d_execu_times,d.group_name,d.group_id  FROM  ad_build_deploy_data a," +
            "(select c.project_id,b.group_name,b.group_id from ad_group b,ad_project c where b.group_id=c.group_id and b.state=1 and b.group_id in(:groupIds)) d WHERE a.project_id(+)=d.project_id and a.create_time(+)> :beginDate and a.create_time(+)< :endDate group by d.group_name,d.group_id order by d.group_id";
        SimpleDateFormat sTime = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat eTime = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss");
        SqlQuery sqlQuery = createSqlQuery(sql);
        Date startDate = null;
        Date endTime = null;
        try {
            startDate = sTime.parse(beginDate);
            endTime = eTime.parse(endDate + " 23:59:59");
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        sqlQuery.setParameter("groupIds", ids).setParameter("beginDate", startDate).setParameter("endDate", endTime);
        List<SqlRow> list = sqlQuery.findList();
        Map<String, String> buildLogMap;
        Map<String, Map<String, String>> retbuildLogMap = new HashMap<>();
        for (SqlRow sqlRow : list) {
            buildLogMap = new HashMap<>();
            buildLogMap.put("b_success_num", "" + sqlRow.get("b_success_num"));
            buildLogMap.put("b_fail_num", "" + sqlRow.get("b_fail_num"));
            buildLogMap.put("b_execu_times", "" + sqlRow.get("b_execu_times"));
            buildLogMap.put("d_success_num", "" + sqlRow.get("d_success_num"));
            buildLogMap.put("d_fail_num", "" + sqlRow.get("d_fail_num"));
            buildLogMap.put("d_execu_times", "" + sqlRow.get("d_execu_times"));
            buildLogMap.put("group_name", "" + sqlRow.get("group_name"));
            buildLogMap.put("group_id", "" + sqlRow.get("group_id"));
            retbuildLogMap.put("" + buildLogMap.get("group_id"), buildLogMap);
        }
        String newSql;
        StringBuilder sb = new StringBuilder();
        String[] groups = groupIds.split(",");
        for (String groupId : groups) {
            sb.append("nvl(sum(decode(t.group_id,").append(groupId).append(",t.b_success_num,null)),0) bsucnum").append(groupId).append(",");
            sb.append("nvl(sum(decode(t.group_id,").append(groupId).append(",t.b_fail_num,null)),0) bfailnum").append(groupId).append(",");
            sb.append("nvl(sum(decode(t.group_id,").append(groupId).append(",t.d_success_num,null)),0) dsucnum").append(groupId).append(",");
            sb.append("nvl(sum(decode(t.group_id,").append(groupId).append(",t.d_fail_num,null)),0) dfailnum").append(groupId).append(",");
        }
        newSql = sb.toString();
        if (sql.length() > 1) {
            newSql = newSql.substring(0, newSql.length() - 1);
        }
        sql = " select t.create_time,"+newSql+" from (" +
            " SELECT nvl(sum(a.b_success_num),0) b_success_num,nvl(sum(a.b_fail_num),0) b_fail_num,nvl(sum(a.d_success_num),0) d_success_num,nvl(sum(a.d_fail_num),0) d_fail_num,d.group_name,d.group_id,trunc(a.create_time) create_time  FROM  ad_build_deploy_data a,(select c.project_id,b.group_name,b.group_id from ad_group b,ad_project c where b.group_id=c.group_id and b.state=1 and b.group_id in(:groupIds)) d WHERE a.project_id(+)=d.project_id and a.create_time> :beginDate and a.create_time< :endDate group by d.group_name,d.group_id,trunc(a.create_time) order by d.group_id) t group by t.create_time order by t.create_time";
        SqlQuery sqlQuery2 = createSqlQuery(sql).setParameter("groupIds", ids).setParameter("beginDate", startDate).setParameter("endDate", endTime);
        List<SqlRow> list2 = sqlQuery2.findList();
        Map<String, String> buildlogDtlMap;
        Map<String, String> deploylogDtlMap;
        List<Map<String, String>> buildlogDtlList;
        List<Map<String, String>> deploylogDtlList;
        Map<String, List<Map<String, String>>> retbuildlogDtlMap = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> retdeploylogDtlMap = new LinkedHashMap<>();
        String createtime;
        for (String groupId : groups) {
            buildlogDtlList = new ArrayList<>();
            deploylogDtlList = new ArrayList<>();
            for (SqlRow sqlRow : list2) {
                buildlogDtlMap = new HashMap<>();
                deploylogDtlMap = new HashMap<>();
                createtime = sqlRow.getString("create_time");
                createtime = createtime.replaceAll("\\/", "-");
                if (createtime.contains("-") && createtime.contains(" ")) {
                    createtime = createtime.substring(createtime.indexOf("-") + 1, createtime.indexOf(" "));
                }
                buildlogDtlMap.put(createtime + "-true", "" + sqlRow.get("bsucnum" + groupId));
                buildlogDtlMap.put(createtime + "-false", "" + sqlRow.get("bfailnum" + groupId));
                deploylogDtlMap.put(createtime + "-true", "" + sqlRow.get("dsucnum" + groupId));
                deploylogDtlMap.put(createtime + "-false", "" + sqlRow.get("dfailnum" + groupId));
                buildlogDtlList.add(buildlogDtlMap);
                deploylogDtlList.add(deploylogDtlMap);
            }
            retbuildlogDtlMap.put("" + groupId, buildlogDtlList);
            retdeploylogDtlMap.put("" + groupId, deploylogDtlList);
        }
        Map<String, Object> retMap;
        List<Map<String, Object>> retList = new ArrayList<>();
        long ll_success;
        long ll_false;
        Map<String, String> buildMap;
        boolean buildBool = false;
        boolean deployBool = false;
        switch (qryType) {
            case 0:
                buildBool = true;
                deployBool = true;
                break;
            case 1:
                buildBool = true;
                break;
            case 2:
                deployBool = true;
                break;
        }
        //根据项目排序
        for (String groupId : groups) {
            //构建
            if (buildBool && retbuildLogMap.containsKey("" + groupId)) {
                retMap = new LinkedHashMap<>();
                buildMap = retbuildLogMap.get("" + groupId);
                ll_success = 0;
                ll_false = 0;
                if (buildMap != null) {
                    retMap.put("productName", buildMap.get("group_name"));
                    retMap.put("link", "构建");
                    retMap.put("success", buildMap.get("b_success_num"));
                    retMap.put("false", buildMap.get("b_fail_num"));
                    if (StringUtils.isNotEmpty((String) retMap.get("success"))) {
                        ll_success = Long.valueOf((String) retMap.get("success"));
                    }
                    if (StringUtils.isNotEmpty((String) retMap.get("false"))) {
                        ll_false = Long.valueOf((String) retMap.get("false"));
                    }
                    retMap.put("allCount", "" + (ll_success + ll_false));
                    if ((ll_success + ll_false) != 0) {
                        retMap.put("proSuccess", "" + (ll_success * 100 / (ll_success + ll_false)) + "%");
                    } else {
                        retMap.put("proSuccess", "0");
                    }
                    retMap.put("duration", buildMap.get("b_execu_times"));
                } else {
                    retMap.put("productName", adGroupImpl.qryById(Long.valueOf(groupId)).getGroupName());
                    retMap.put("link", "构建");
                    retMap.put("success", "0");
                    retMap.put("false", "0");
                    retMap.put("allCount", "0");
                    retMap.put("proSuccess", "0");
                    retMap.put("duration", "0");
                }
                List<String> dateList = DateConvertUtils.getDates(beginDate, endDate);
                retMap.put("date", dateList);
                List<String> successList = new ArrayList<>();
                List<String> failureList = new ArrayList<>();
                if (dateList != null) {
                    for (int i = 0; i < dateList.size(); i++) {
                        successList.add("0");
                        failureList.add("0");
                    }

                }
                Map<String, String> middleMap = new LinkedHashMap<>();
                if (dateList != null) {
                    for (String d1 : dateList) {
                        if (!retMap.containsKey(d1 + "-true")) {
                            middleMap.put(d1 + "-true", "0");
                            middleMap.put(d1 + "-false", "0");
                        }
                    }

                }
                int jj = 0;
                if (retbuildlogDtlMap.containsKey("" + groupId)) {
                    List<Map<String, String>> rebuildList = retbuildlogDtlMap.get("" + groupId);
                    if (rebuildList != null) {
                        for (Map<String, String> hmap : rebuildList) {
                            for (Map.Entry<String, String> entry : middleMap.entrySet()) {
                                if (hmap.containsKey(entry.getKey())) {
                                    middleMap.put(entry.getKey(), hmap.get(entry.getKey()));
                                    if (!("0").equals(hmap.get(entry.getKey())) && entry.getKey().indexOf("true") > 0) {
                                        jj++;
                                    }
                                }
                            }
                        }
                    }
                }
                if (retbuildlogDtlMap.containsKey("" + groupId)) {
                    List<Map<String, String>> rebuildList = retbuildlogDtlMap.get("" + groupId);
                    if (rebuildList != null) {
                        for (Map<String, String> hmap : rebuildList) {
                            for (int i = 0; i < dateList.size(); i++) {
                                if (hmap.containsKey(dateList.get(i) + "-true")) {
                                    successList.set(i, hmap.get(dateList.get(i) + "-true"));
                                }
                                if (hmap.containsKey(dateList.get(i) + "-false")) {
                                    failureList.set(i, hmap.get(dateList.get(i) + "-false"));
                                }
                            }
                        }
                    }
                }
                retMap.put("successList", successList);
                retMap.put("failureList", failureList);
                if (jj > 0 && (retMap.get("duration") != null && StringUtils.isNotEmpty((String) retMap.get("duration")))) {
                    BigDecimal b = new BigDecimal(Float.valueOf((String) retMap.get("duration")) / jj);
                    float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                    retMap.put("duration", "" + f1);
                }
                retList.add(retMap);
            }
            //部署
            if (deployBool && retbuildLogMap.containsKey("" + groupId)) {
                retMap = new LinkedHashMap<>();
                buildMap = retbuildLogMap.get("" + groupId);
                ll_success = 0;
                ll_false = 0;
                if (buildMap != null) {
                    retMap = new LinkedHashMap<>();
                    retMap.put("productName", buildMap.get("group_name"));
                    retMap.put("link", "发布");
                    retMap.put("success", buildMap.get("d_success_num"));
                    retMap.put("false", buildMap.get("d_fail_num"));
                    if (StringUtils.isNotEmpty((String) retMap.get("success"))) {
                        ll_success = Long.valueOf((String) retMap.get("success"));
                    }
                    if (StringUtils.isNotEmpty((String) retMap.get("false"))) {
                        ll_false = Long.valueOf((String) retMap.get("false"));
                    }
                    retMap.put("allCount", "" + (ll_success + ll_false));
                    if ((ll_success + ll_false) != 0) {
                        retMap.put("proSuccess", "" + (ll_success * 100 / (ll_success + ll_false)) + "%");
                    } else {
                        retMap.put("proSuccess", "0");
                    }
                    retMap.put("duration", buildMap.get("d_execu_times"));
                } else {
                    retMap.put("productName", adGroupImpl.qryById(Long.valueOf(groupId)).getGroupName());
                    retMap.put("link", "发布");
                    retMap.put("success", "0");
                    retMap.put("false", "0");
                    retMap.put("allCount", "0");
                    retMap.put("proSuccess", "0");
                    retMap.put("duration", "0");
                }
                List<String> dateList = DateConvertUtils.getDates(beginDate, endDate);
                retMap.put("date", dateList);
                List<String> successList = new ArrayList<>();
                List<String> failureList = new ArrayList<>();
                if (dateList != null) {
                    for (int i = 0; i < dateList.size(); i++) {
                        successList.add("0");
                        failureList.add("0");
                    }

                }
                Map<String, String> middleMap = new LinkedHashMap<>();
                if (dateList != null) {
                    for (String d1 : dateList) {
                        if (!retMap.containsKey(d1 + "-true")) {
                            middleMap.put(d1 + "-true", "0");
                            middleMap.put(d1 + "-false", "0");
                        }
                    }

                }
                int kk = 0;
                if (retdeploylogDtlMap.containsKey("" + groupId)) {
                    List<Map<String, String>> redeployList = retdeploylogDtlMap.get("" + groupId);
                    if (redeployList != null) {
                        for (Map<String, String> hmap : redeployList) {
                            for (Map.Entry<String, String> entry : middleMap.entrySet()) {
                                if (hmap.containsKey(entry.getKey())) {
                                    middleMap.put(entry.getKey(), hmap.get(entry.getKey()));
                                    if (!("0").equals(hmap.get(entry.getKey())) && entry.getKey().indexOf("true") > 0) {
                                        kk++;
                                    }
                                }
                            }
                        }
                    }
                }
                if (retdeploylogDtlMap.containsKey("" + groupId)) {
                    List<Map<String, String>> redeployList = retdeploylogDtlMap.get("" + groupId);
                    if (redeployList != null) {
                        for (Map<String, String> hmap : redeployList) {
                            for (int i = 0; i < dateList.size(); i++) {
                                if (hmap.containsKey(dateList.get(i) + "-true")) {
                                    successList.set(i, hmap.get(dateList.get(i) + "-true"));
                                }
                                if (hmap.containsKey(dateList.get(i) + "-false")) {
                                    failureList.set(i, hmap.get(dateList.get(i) + "-false"));
                                }
                            }
                        }
                    }
                }
                retMap.put("successList", successList);
                retMap.put("failureList", failureList);
                if (kk > 0 && (retMap.get("duration") != null && StringUtils.isNotEmpty((String) retMap.get("duration")))) {
                    BigDecimal b = new BigDecimal(Float.valueOf((String) retMap.get("duration")) / kk);
                    float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                    retMap.put("duration", "" + f1);
                }
                retList.add(retMap);
            }
        }
        return retList;
    }
}
