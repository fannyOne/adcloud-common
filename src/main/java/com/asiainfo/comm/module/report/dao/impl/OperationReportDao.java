package com.asiainfo.comm.module.report.dao.impl;

import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by zhenghp on 2016/11/9.
 */
@lombok.extern.slf4j.Slf4j
@Component
public class OperationReportDao {
    public List<SqlRow> countGroupUser() {
        String sql = "select g.group_id groupId,nvl(report.count,0) count from ad_group g  left join \n" +
            "(select group_id groupId,count(1) count from ad_group_user where state=1 group by group_id) report\n" +
            "on report.groupId=g.group_id where g.state=1";
        SqlQuery sqlQuery = createSqlQuery(sql);
        return sqlQuery.findList();
    }

    public List<SqlRow> countBranch() {
        String sql = " select g.group_id groupId,nvl(report.count,0) count from ad_group g  left join \n" +
            "(select b.group_id groupId,count(1) count from ad_branch a,ad_project b  where a.state=1 and b.state=1\n" +
            "and a.project_id=b.project_id group by b.group_id) report\n" +
            "on report.groupId=g.group_id where g.state=1";
        SqlQuery sqlQuery = createSqlQuery(sql);
        return sqlQuery.findList();
    }

    public List<SqlRow> countEnv() {
        String sql = "  select g.group_id groupId,nvl(report.count,0) count from ad_group g  left join \n" +
            "(select b.group_id groupId,count(1) count from ad_branch a,ad_project b  where a.state=1 and b.state=1\n" +
            "and a.project_id=b.project_id and\n" +
            "not exists (select * from ad_stage c where c.state=1 and a.branch_id = c.branch_id and c.stage_code=4)\n" +
            "group by b.group_id) report\n" +
            "on report.groupId=g.group_id where g.state=1";
        SqlQuery sqlQuery = createSqlQuery(sql);
        return sqlQuery.findList();
    }

    public List<SqlRow> countBuild() {
        String sql = "select g.group_id groupId,nvl(report.count,0) count from ad_group g  left join \n" +
            "(select b.group_id groupId,count(1) count from ad_branch a,ad_project b ,ad_build_log c where a.state=1 and b.state=1\n" +
            "and a.project_id=b.project_id and c.state=1 and c.branch_id=a.branch_id group by b.group_id) report\n" +
            "on report.groupId=g.group_id where g.state=1";
        SqlQuery sqlQuery = createSqlQuery(sql);
        return sqlQuery.findList();
    }

    public List<SqlRow> countBuildAvgTime() {
        String sql = "select g.group_id groupId,nvl(report.count,0) count from ad_group g  left join \n" +
            "(select b.group_id groupId,avg(c.build_date-c.create_date)*24*3600 count from ad_branch a,ad_project b ,ad_build_log c where a.state=1 and b.state=1\n" +
            "and a.project_id=b.project_id and c.state=1 and c.branch_id=a.branch_id and c.build_result=2\n" +
            "group by b.group_id) report\n" +
            "on report.groupId=g.group_id where g.state=1";
        SqlQuery sqlQuery = createSqlQuery(sql);
        return sqlQuery.findList();
    }

}
