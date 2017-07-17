package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdSonarData;
import com.asiainfo.comm.module.models.AdSonarReport;
import com.asiainfo.comm.module.models.query.QAdSonarData;
import com.asiainfo.comm.module.models.query.QAdSonarReport;
import com.avaje.ebean.*;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by zhangpeng on 2016/7/6.
 */
@Component
public class AdSonarReportDAO {
    private String qrySonarDateReport = "SELECT R.*,G.GROUP_NAME\n" +
        "  FROM AD_SONAR_DATA R, AD_GROUP G, AD_TREE_DATA T\n" +
        " WHERE G.STATE = 1\n" +
        "   AND T.STATE = 1\n" +
        "   AND G.GROUP_ID = CAST(T.TREE_CODE AS NUMBER)\n" +
        "   AND T.TREE_TYPE = 2\n" +
        "   AND R.PROJECT_NAME = T.TREE_PARA\n" +
        "   AND TO_CHAR(R.SCAN_DATE,'YYYY-MM-DD') = :date\n" +
        "   AND G.GROUP_ID IN (:groupIdList) ORDER BY G.GROUP_NAME\n";

    private String qrySonarDateReportSelect = "SELECT R.*,G.GROUP_NAME\n" +
        "  FROM AD_SONAR_DATA R, AD_GROUP G, AD_TREE_DATA T\n" +
        " WHERE G.STATE = 1\n" +
        "   AND T.STATE = 1\n" +
        "   AND G.GROUP_ID = CAST(T.TREE_CODE AS NUMBER)\n" +
        "   AND T.TREE_TYPE = 2\n" +
        "   AND R.PROJECT_NAME = T.TREE_PARA\n" +
        "   AND TO_CHAR(R.SCAN_DATE,'YYYY-MM-DD') = :date\n" +
        "   AND G.GROUP_ID IN (:groupIdList) AND G.GROUP_ID IN (SELECT G.GROUP_ID\n" +
        "  FROM AD_SONAR_DATA R, AD_GROUP G, AD_TREE_DATA T\n" +
        " WHERE G.STATE = 1\n" +
        "   AND T.STATE = 1\n" +
        "   AND G.GROUP_ID = CAST(T.TREE_CODE AS NUMBER)\n" +
        "   AND T.TREE_TYPE = 2\n" +
        "   AND R.PROJECT_NAME = T.TREE_PARA\n" +
        "   AND TO_CHAR(R.SCAN_DATE,'YYYY-MM-DD') = :startDate\n" +
        "   AND G.GROUP_ID IN (:groupIdList)) ORDER BY G.GROUP_NAME\n";

    public List<AdSonarReport> qryById(Long id) {
        List<AdSonarReport> adSonarReports = new QAdSonarReport().id.eq(id).findList();
        return adSonarReports;
    }

    public void delBySonarReportID(Long sonar_id) {
        String sql = "delete ad_sonar_report t where t.sonar_id= :sonar_id ";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("sonar_id", sonar_id);
        Ebean.execute(update);
    }

    public void delBySonarReptId(Long id) {
        String sql = "delete ad_sonar_report t where t.id= :id ";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("id", id);
        Ebean.execute(update);
    }

    public PagedList<AdSonarReport> getAdSonarReportCruIdByName(String project_name, Long type) {  // TODO 根据名字type，查最近二条周报或月报
        PagedList<AdSonarReport> soanrList = new QAdSonarReport().projectName.like(project_name).reports.eq(type).orderBy("scan_date desc").findPagedList(0, 2);
        return soanrList;
    }

    public void getAdSonarReportByDateAndType(String project_name, long reports) {
        String sql = "select t.id from ad_sonar_report t where t.project_name= :project_name and t.reports= :reports and to_char(t.scan_date,'yyyy-mm-dd') =to_char(sysdate,'yyyy-mm-dd')";
        SqlQuery sqlQuery = createSqlQuery(sql);
        sqlQuery.setParameter("project_name", project_name);
        sqlQuery.setParameter("reports", reports);
        List<SqlRow> list = sqlQuery.findList();
        for (SqlRow sqlRow : list) {
            delBySonarReptId(sqlRow.getLong("id"));
            System.out.println("删除成功 Ad_sonar_report！！！！");
        }
    }

    public List<SqlRow> getAdSonarReportByNameAndDate(String project_name, Long type, String date) {  // TODO 根据名字type和日期，查周报或月报
        String sql = "select t.* from ad_sonar_report t where t.project_name= :project_name and t.reports= :type and to_char(t.scan_date,'yyyy-MM-dd')= :date";
        SqlQuery sqlQuery = createSqlQuery(sql);
        sqlQuery.setParameter("project_name", project_name);
        sqlQuery.setParameter("type", type);
        sqlQuery.setParameter("date", date);
        List<SqlRow> list = sqlQuery.findList();
        return list;
    }

    public List<SqlRow> getAdReportByNameAndDate(String project_name, Long type, String date) {
        // TODO 根据名字type和日期，查周报或月报
        String sql = "select t.* from ad_sonar_report t where t.project_name= :project_name and t.reports= :type and to_char(t.scan_date,'yyyy-MM')= :date";
        SqlQuery sqlQuery = createSqlQuery(sql);
        sqlQuery.setParameter("project_name", project_name);
        sqlQuery.setParameter("type", type);
        sqlQuery.setParameter("date", date);
        List<SqlRow> list = sqlQuery.findList();
        return list;
    }

    public List<AdSonarData> obtSonarDataSonarId(Long sonarId) {
        List<AdSonarData> soanrList = new QAdSonarData().id.eq(sonarId).findList();
        return soanrList;
    }

    public List<SqlRow> qrySonarDate(String date, String groupIdList) {
        String sql = qrySonarDateReport.replace(":groupIdList", groupIdList);
        return Ebean.createSqlQuery(sql)
            .setParameter("date", date).findList();
    }

    public List<SqlRow> qrySonarDateFilter(String startDate, String endDate, String groupIdList) {
        String sql = qrySonarDateReportSelect.replace(":groupIdList", groupIdList);
        return Ebean.createSqlQuery(sql).setParameter("startDate", startDate)
            .setParameter("date", endDate).findList();
    }
}
