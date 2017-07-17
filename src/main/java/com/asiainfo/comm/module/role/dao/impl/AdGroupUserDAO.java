package com.asiainfo.comm.module.role.dao.impl;

import com.asiainfo.comm.module.models.AdGroupUser;
import com.asiainfo.comm.module.models.functionModels.QLAdGroupUser;
import com.asiainfo.comm.module.models.functionModels.query.QQLAdGroupUser;
import com.asiainfo.comm.module.models.query.QAdGroupUser;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by zhenghp on 2016/8/21.
 */
@Component
public class AdGroupUserDAO {


    public List<AdGroupUser> qryByUserName(String userName) {
        return new QAdGroupUser().state.eq(1).userName.eq(userName).findList();
    }

    public List<AdGroupUser> qryByGroupId(long groupId) {
        return new QAdGroupUser().state.eq(1).groupId.eq(groupId).findList();
    }

    public List<AdGroupUser> qryByGroupIdAndUserName(long groupId, String userName) {
        return new QAdGroupUser().state.eq(1).groupId.eq(groupId).userName.eq(userName).findList();
    }

    public List<QLAdGroupUser> qryLByUserName(String username) {
        return new QQLAdGroupUser().state.eq(1).userName.eq(username).findList();
    }

    public List<AdGroupUser> delete(List<AdGroupUser> adGroupUsers) {
        for (AdGroupUser adGroupUser : adGroupUsers) {
            adGroupUser.setState(0);
            Ebean.save(adGroupUser);
        }
        return adGroupUsers;
    }

    public List<AdGroupUser> save(List<AdGroupUser> adGroupUsers) {
        for (AdGroupUser adGroupUser : adGroupUsers) {
            save(adGroupUser);
        }
        return adGroupUsers;
    }

    public AdGroupUser save(AdGroupUser adGroupUser) {
        adGroupUser.setState(1);
        adGroupUser.setCreateDate(new Date());
        adGroupUser.setUpdateDate(new Date());
        Ebean.save(adGroupUser);
        return adGroupUser;
    }

    public AdGroupUser update(AdGroupUser adGroupUser) {
        adGroupUser.setUpdateDate(new Date());
        Ebean.save(adGroupUser);
        return adGroupUser;
    }

    //根据用户名模糊查找项目下的成员
    public List<SqlRow> qryByUserNameAndGroupId(String userName, long groupId) {
        StringBuilder sql = new StringBuilder("");
        sql.append("select a.user_id, a.login_name, a.display_name, a.login4a_name ")
            .append("  from AD_USER a, AD_GROUP_USER b")
            .append(" where b.group_id = :group_id")
            .append("   and a.login_name= b.user_name")
            .append("   and b.state = '1'")
            .append("   and a.state = '1'")
            .append("   and b.user_name like :user_name");
        SqlQuery sqlQuery = createSqlQuery(sql.toString())
            .setParameter("group_id", groupId)
            .setParameter("user_name", "%" + userName + "%");
        return sqlQuery.findList();
    }
}
