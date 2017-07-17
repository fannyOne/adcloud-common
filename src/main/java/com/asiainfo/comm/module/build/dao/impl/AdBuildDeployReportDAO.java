package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdBuildDeployData;
import com.asiainfo.comm.module.models.AdBuildDeployReport;
import com.asiainfo.comm.module.models.query.QAdBuildDeployData;
import com.asiainfo.comm.module.models.query.QAdBuildDeployReport;
import com.avaje.ebean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by zhangpeng on 2016/7/12.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class AdBuildDeployReportDAO {
    @Autowired
    AdBuildDeployDataDAO adBuildDeployDataDAO;

    public void deleBuildReportDataByName(String project_name, Double type) {//TODO 根据名字和type删除deploy_report最近一条数据
        String sql = "delete ad_build_deploy_report t where t.project_name = :project_name and t.type= :type and to_char(t.end_date,'yyyy-MM-dd')>to_char(sysdate-1,'yyyy-MM-dd')";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("project_name", project_name);
        update.setParameter("type", type);
        Ebean.execute(update);
    }

    public String getMonthlyFirstDay(String date) { //TODO 获取本月第一天
        SimpleDateFormat format = new SimpleDateFormat(date);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        String first = format.format(c.getTime());
        System.out.println("===============firstDay:" + first);
        return first;
    }


    public List<AdBuildDeployData> getWeekData(long id) {//TODO 获取周报中每天的信息
        System.out.println("id====" + id);
        List<AdBuildDeployData> adBuildDeployDatas = new QAdBuildDeployData().ext1.eq(id).orderBy("create_time desc").findList();

        System.out.println("第一个id为：" + adBuildDeployDatas.get(0).getId());
        System.out.println("最一个id为：" + adBuildDeployDatas.get(adBuildDeployDatas.size() - 1).getId());
        System.out.println("大小为：" + adBuildDeployDatas.size());
        return adBuildDeployDatas;
    }

    public List<SqlRow> getMonthData(String project_name, String date) { //TODO 获取月报中每周的信息
        String sql = "select t.* from ad_build_deploy_data t where t.project_name= :project_name and to_char(t.create_time,'yyyy-MM')= :date  order by t.create_time desc";
        SqlQuery sqlQuery1 = createSqlQuery(sql);
        sqlQuery1.setParameter("project_name", project_name);
        sqlQuery1.setParameter("date", date);
        List<SqlRow> list = sqlQuery1.findList();
        return list;
    }

    public int getLastDayOfMonth(int year, int month) {//TODO 获取某年某月的最后一天
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR, year);
        //设置月份
        cal.set(Calendar.MONTH, month - 1);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());
        log.error("lastDayOfMonth-" + lastDayOfMonth);
        return lastDay;
    }

    public List<SqlRow> getBuildDeployReportTenData(String project_name, String date) {//TODO 根据日期查询最近10条记录
        String sql = "select t.* from ad_build_deploy_report t where t.type= 1 and t.project_name= :project_name and \n" +
            " to_char(t.end_date,'yyyy-MM-dd') <=:date  and rownum<=10 order by t.end_date";
        SqlQuery sqlQuery1 = createSqlQuery(sql);
        sqlQuery1.setParameter("project_name", project_name);
        sqlQuery1.setParameter("date", date);
        List<SqlRow> list = sqlQuery1.findList();
        return list;
    }

    public List<SqlRow> getMonthyDataByDate(String name, String date) {//TODO 根据日期获取相应的月报
        String sql = " select t.* from ad_build_deploy_report t where t.type= 2 and t.project_name= :name and \n" +
            " :date between  to_char(t.begin_date,'yyyy-MM-dd') and  to_char(t.end_date,'yyyy-MM-dd')";
        SqlQuery sqlQuery1 = createSqlQuery(sql);
        sqlQuery1.setParameter("name", name);
        sqlQuery1.setParameter("date", date);
        List<SqlRow> list = sqlQuery1.findList();
        return list;
    }
}
