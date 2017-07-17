package com.asiainfo.comm.module.build.dao.impl;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import org.springframework.stereotype.Component;

/**
 * Created by zhangpeng on 2016/12/28.
 */
@Component
public class AdTaskCaseNumberDAO {

    public void deleteByDate(String date) {
        String sql = "delete AD_TASK_CASE_NUMBER t where to_char(t.online_date,'yyyyMM')= :date";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("date", date);
        Ebean.execute(update);
    }
}
