package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdUser;
import com.asiainfo.comm.module.models.query.QAdUser;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by weif on 2016/6/15.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class AdUserDAO {

    public AdUser getUserById(long userId) {
        return new QAdUser().userId.eq(userId).findUnique();
    }

    public AdUser getUserByLoginName(String name) {
        return new QAdUser().loginName.eq(name).state.eq(1).findUnique();
    }

    public List<AdUser> qryUserByLoginName(String name) {
        return new QAdUser().loginName.eq(name).state.eq(1).findList();
    }

    public long countUserCreateDate(Date date) {
        return new QAdUser().loginName.isNotNull().state.eq(1).createDate.after(date).findRowCount();
    }

    public long countUsers() {
        return new QAdUser().loginName.isNotNull().state.eq(1).findRowCount();
    }


    public List<AdUser> qryUserSqlRowByProject(String projectid) {
        List<AdUser> adUserList = Lists.newArrayList();
        String sql = "select * from ad_user where user_id in (select distinct(c.op_id) from ad_systemdeploy_log c \n" +
            " where c.project_id = :projectid )";
        SqlQuery sqlQuery = createSqlQuery(sql);
        sqlQuery.setParameter("projectid", projectid);
        List<SqlRow> sqlRow = sqlQuery.findList();
        initUserInfo(adUserList, sqlRow);
        return adUserList;
    }

    public void initUserInfo(List<AdUser> adUserList, List<SqlRow> sqlRow) {
        if (null != sqlRow && sqlRow.size() > 0) {
            for (SqlRow row : sqlRow) {
                AdUser user = new AdUser();
                user.setUserId(row.getLong("USER_ID"));
                user.setLoginName(row.getString("LOGIN_NAME"));
                user.setDisplayName(row.getString("DISPLAY_NAME"));
                adUserList.add(user);
            }
        }
    }

    public AdUser create(String longinName, String displayName) {
        AdUser user = new AdUser();
        user.setLoginName(longinName);
        user.setDisplayName(displayName);
        user.setState(1L);
        user.setCreateDate(new Date());
        user.save();
        return user;
    }

    public void update(AdUser user) {
        user.save();
    }

    public void updateUserFirst(int firstLogin, long userId) {
        AdUser user = getUserById(userId);
        user.setFirstLogin(firstLogin);
        user.save();
    }

    public int qryOnlineNumber(int invalidTime) {
        Date date = new Date(new Date().getTime() - invalidTime * 1000);
        return new QAdUser().sessionId.isNotNull().activeDate.after(date).findRowCount();
    }


}
