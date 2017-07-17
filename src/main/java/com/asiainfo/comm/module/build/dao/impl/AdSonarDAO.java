package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdSonarData;
import com.asiainfo.comm.module.models.AdSonarReport;
import com.asiainfo.comm.module.models.AdParaDetail;
import com.asiainfo.comm.module.models.query.QAdSonarData;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.avaje.ebean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static com.avaje.ebean.Ebean.createSqlQuery;


/**
 * Created by zhangpeng on 2016/7/5.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class AdSonarDAO {

    @Autowired
    AdParaDetailDAO paraDetailDAO;
    @Autowired
    AdSonarReportDAO adSonarReportDAO;

    public List<AdSonarData> QryAdSonardata() {
        List<AdSonarData> sonarDatas = new QAdSonarData().findList();
        return sonarDatas;
    }

    public List<AdSonarData> qryAdSonarDataById(Long id) {
        List<AdSonarData> sonarDatas = new QAdSonarData().id.eq(id).findList();
        return sonarDatas;
    }

    public PagedList<AdSonarData> QryAdSonarDataByProjectName(String project_name) {
        // TODO 根据名字，查最近两条记录
        PagedList<AdSonarData> soanrList = new QAdSonarData().projectName.like(project_name).orderBy("scan_date desc").findPagedList(0, 2);

        return soanrList;
    }

    public List<AdSonarData> qryAdSonarByName(String project_name) {
        // TODO 根据名字，查最近一条记录
        List<AdSonarData> soanrList = new QAdSonarData().projectName.like(project_name).orderBy("scan_date desc").findList();

        return soanrList;
    }

    public Boolean obtAdSonarDara() {

        // TODO 获取sonar扫出的数据
        boolean returnBool = false;
        String sql = "select m.project_id,m.name,\n" +
            "sum(decode(c.METRIC_ID, '10006', c.VALUE, 0)) FILENUMS, --文件数   Number of files\n" +
            "sum(decode(c.METRIC_ID, '10009', c.VALUE, 0)) METHODNUMS, --方法数  Functions\n" +
            "sum(decode(c.METRIC_ID, '10002', c.VALUE, 0)) CODELINES, --代码行数   Non Commenting Lines of Code\n" +
            "sum(decode(c.METRIC_ID, '10089', c.VALUE, 0)) REPEAT, --重复率   duplicated_lines_density\n" +
            "sum(decode(c.METRIC_ID, '10024', c.VALUE, 0)) METHOD_COMP, --方法复杂度  Complexity average by function\n" +
            "sum(decode(c.METRIC_ID,  '10019', c.VALUE, 0)) D_COMPLEXITY, --总复杂度  Cyclomatic complexity\n" +
            "sum(decode(c.METRIC_ID, '10093', c.VALUE, 0)) SERIOUS_ISSUES, --严重类问题\n" +
            "sum(decode(c.METRIC_ID, '10092', c.VALUE, 0)) BLOCK_ISSUES, --阻断类问题\n" +
            "sum(decode(c.METRIC_ID, '10035', c.VALUE, 0)) COVERAGE,  --百分号 (覆盖率)\n" +
            "sum(decode(c.METRIC_ID, '10033', c.VALUE, 0)) UNIT_SUCCESS_RATE, --百分号% (单元测试成功率)\n" +
            "sum(decode(c.METRIC_ID, '10028', c.VALUE, 0)) UNITNUMS, --单元测试个数\n" +
            "sum(decode(c.METRIC_ID, '10029', c.VALUE, 0)) UNITTIME  --单元测试执行时长\n" +
            " from (select b.id   project_id,\n" +
            "       b.name,\n" +
            "       a.id    SNAPSHOT_id,\n" +
            "       a.created_at,\n" +
            "       a.period2_param\n" +
            "from devdb.SNAPSHOTS a, devdb.projects b\n" +
            "where a.project_id = b.id and b.scope = 'PRJ' AND b.qualifier = 'TRK' and a.scope = 'PRJ' and a.islast = 1) m,devdb.PROJECT_MEASURES c\n" +
            "where c.snapshot_id=m.snapshot_id and  c.metric_id in \n" +
            "(10006,\n" +
            " 10009,\n" +
            " 10002,\n" +
            " 10089,\n" +
            " 10024,\n" +
            " 10019,\n" +
            " 10093,\n" +
            " 100932,\n" +
            " 10035,\n" +
            " 10033,\n" +
            " 10028,\n" +
            " 10029\n" +
            " )group by m.project_id,m.name";
        Connection connDo = null;
        Statement pstmtDo = null;
        //int branchNum = 0;
        ResultSet rs = null;
        DecimalFormat df = new DecimalFormat("#.0");
        //要加delete
        try {
            connDo = getConnection("SONAR");

            pstmtDo = connDo.createStatement();
            rs = pstmtDo.executeQuery(sql);
            java.util.Date syncTime = new java.util.Date();
            while (rs.next()) {
                getSonarDataIdByProjectNamedel(rs.getString("name"));
                AdSonarData data = new AdSonarData();
                data.setProjectName(rs.getString("name"));
                data.setFilenums(rs.getLong("FILENUMS"));
                data.setMethodnums(rs.getLong("METHODNUMS"));
                data.setCodelines(rs.getLong("CODELINES"));
                data.setRepeat(rs.getDouble("REPEAT"));
                data.setMethodComp(rs.getDouble("METHOD_COMP"));
                data.setDComplexity(rs.getLong("D_COMPLEXITY"));
                data.setSeriousIssues(rs.getLong("SERIOUS_ISSUES"));
                data.setBlockIssues(rs.getLong("BLOCK_ISSUES"));
                data.setCoverage(rs.getDouble("COVERAGE"));
                data.setUnitSuccessRate(rs.getDouble("UNIT_SUCCESS_RATE"));
                data.setUnitnums(rs.getLong("UNITNUMS"));
                data.setUnittime(Double.parseDouble(df.format(rs.getDouble("UNITTIME") / 1000)));
                data.setScanDate(syncTime);
                data.save();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connDo != null) {
                try {
                    connDo.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (pstmtDo != null) {
                try {
                    pstmtDo.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
        return true;
    }


    public Connection getConnection(String databaseName) throws SQLException {
        AdParaDetail paraDetail = paraDetailDAO.qryByDetails("X", databaseName.toUpperCase() + "_DB_INFO", databaseName.toUpperCase() + "_DB_INFO").get(0);
        return DriverManager.getConnection(paraDetail.getPara1(), paraDetail.getPara2(), paraDetail.getPara3());
    }

    public int getCurData() {   // TODO 获取当前星期几 和 当天是不是最当月的最后一天
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd EEEE");
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        int weeks = ca.get(Calendar.DAY_OF_WEEK);
        int days = ca.get(Calendar.DAY_OF_MONTH);
        ca.set(Calendar.DAY_OF_WEEK, ca.get(Calendar.DAY_OF_WEEK));
        int lastDay = ca.getActualMaximum(Calendar.DAY_OF_MONTH);
        log.error("这个月的最后一天是：" + lastDay);
        log.error("今天是：" + weeks + "-------" + format.format(ca.getTime()) + "======days:" + days);
        if (days == lastDay && weeks == 6) { // TODO 判断今天是否是最后一天，并且是周五
            return 2;
        } else if (days == lastDay) { // TODO 判断今天是不是最后一天
            return 3;
        } else if (weeks == 6) { //TODO 判断今天是不是周五
            return 5;
        } else if (weeks == 1) { //TODO 判断今天是不是周日
            return 7;
        } else {
            return 1;
        }
    }

    public void delBySonarDataID(Long id) {
        String sql = "delete ad_sonar_data t where t.id= :id ";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("id", id);
        Ebean.execute(update);
    }

    public void getSonarDataIdByProjectNamedel(String project_name) {
        String sql = "select t.id from ad_sonar_data t where t.project_name= :project_name and to_char(t.scan_date,'yyyy-mm-dd')>to_char(sysdate-1,'yyyy-mm-dd')";
        SqlQuery sqlQuery = createSqlQuery(sql);
        sqlQuery.setParameter("project_name", project_name);
        List<SqlRow> list = sqlQuery.findList();
        for (SqlRow sqlRow : list) {
            System.out.println("1111111111111111111111111111");
            System.out.println(sqlRow.getLong("id"));
            adSonarReportDAO.delBySonarReportID(sqlRow.getLong("id"));
            delBySonarDataID(sqlRow.getLong("id"));
            log.error("adSonarReport 和adSonarData " + sqlRow.getLong("id") + "删除成功！！！！");
        }
    }

    public void test() {

    }

    public List<AdSonarReport> getAdSonarReportDataByName(String name, Long type) {
        PagedList<AdSonarReport> adSonarReportsPage = adSonarReportDAO.getAdSonarReportCruIdByName(name, type);
        List<AdSonarReport> adSonarReports = adSonarReportsPage.getList();
        return adSonarReports;
    }

    public Map getWeekDate(String sDate) {//TODO 根据传入的日期计算这周的周一和周五的日期
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Map map = new HashMap<>();
        Date date;
        try {
            date = format.parse(sDate);
            Calendar ca = Calendar.getInstance();
            ca.setTime(date);
            int day = ca.get(Calendar.DAY_OF_WEEK);
            if (day == 1) {
                day = day + 7;
            }
            int first = -(day + 5) % 7;//计算周一
            log.error("a======first" + first + "====day" + day);
            ca.add(Calendar.DAY_OF_MONTH, first);
            log.error("周一days1：" + format.format(ca.getTime()));
            map.put("monday", format.format(ca.getTime()));
            ca.setTime(date);
            int second = 6 - day;//计算周五
            log.error("a======second" + second + "====day" + day);
            ca.add(Calendar.DAY_OF_MONTH, second);
            log.error("周五days2：" + format.format(ca.getTime()));
            map.put("friday", format.format(ca.getTime()));
            ca.setTime(date);
            int last = 8 - day;//计算周日
            log.error("a======last" + last + "====day" + day);
            ca.add(Calendar.DAY_OF_MONTH, last);
            log.error("周日days3：" + format.format(ca.getTime()));
            map.put("sunday", format.format(ca.getTime()));
            return map;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Date getEveryDate(String sDate, int day) {//TODO 获取前后几天的日期
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = format.parse(sDate);
            Calendar ca = Calendar.getInstance();
            ca.setTime(date);
            ca.add(Calendar.DAY_OF_MONTH, day);
            return ca.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            log.error("传入的日期为空：getEveryDate");
            return null;
        }
    }

    public String getMonthDate(String sDate, int day) {//TODO  获取当月的前后几个月份
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Date date;
        try {
            date = format.parse(sDate);
            Calendar ca = Calendar.getInstance();
            ca.setTime(date);
            ca.add(Calendar.MONTH, day);
            log.error("sDate===" + sDate + "day==" + date + "日期为:" + format.format(ca.getTime()));
            return format.format(ca.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            log.error("传入的日期为空：getMonthDate");
            return null;
        }
    }
}
