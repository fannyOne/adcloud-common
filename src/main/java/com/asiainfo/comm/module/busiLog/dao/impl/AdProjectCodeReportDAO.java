package com.asiainfo.comm.module.busiLog.dao.impl;

import com.asiainfo.comm.module.models.AdProjectCodeReport;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by weif on 2016/12/30.
 */
@Component
public class AdProjectCodeReportDAO {

    public void saveAll(List<AdProjectCodeReport> adProjectCodeReports) {
        if (!CollectionUtils.isEmpty(adProjectCodeReports)) {
            Ebean.saveAll(adProjectCodeReports);
        }
    }

    public long qryCountAllFileNum() {
        String sql = "SELECT sum(file_num) p\n" +
            "  from (select *\n" +
            "          from ad_project_code_report a\n" +
            "         where (a.create_date, a.project_id) in\n" +
            "               (SELECT max(t.create_date), t.project_id\n" +
            "                  FROM ad_project_code_report t\n" +
            "                 group by t.project_id))\n";
        SqlRow fileNum = Ebean.createSqlQuery(sql).findUnique();
        return fileNum.getLong("p");
    }
}
