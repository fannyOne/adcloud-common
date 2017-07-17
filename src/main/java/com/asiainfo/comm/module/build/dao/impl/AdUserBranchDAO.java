package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdUser;
import com.asiainfo.comm.module.models.AdUserBranch;
import com.asiainfo.comm.module.models.query.QAdUserBranch;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.SqlRow;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by YangRY on 2016/8/30.
 */
@Component
public class AdUserBranchDAO {
    public List<SqlRow> qryWorkingSpaceByUser(Long userId) {
        List<SqlRow> list = Ebean.createSqlQuery("SELECT G.GROUP_NAME,P.PROJECT_NAME,B.BRANCH_ID,B.BRANCH_NAME,B.BRANCH_DESC\n" +
            "  FROM AD_USER_BRANCH   UB,\n" +
            "       AD_GROUP_USER    GU,\n" +
            "       AD_BRANCH        B,\n" +
            "       AD_PROJECT      P,\n" +
            "       AD_GROUP         G,\n" +
            "       AD_USER          U\n" +
            " WHERE UB.STATE = 1 \n" +
            "   AND GU.STATE = 1\n" +
            "   AND B.STATE = 1\n" +
            "   AND P.STATE = 1\n" +
            "   AND G.STATE = 1\n" +
            "   AND G.GROUP_ID = GU.GROUP_ID\n" +
            "   AND G.GROUP_ID = P.GROUP_ID\n" +
            "   AND P.PROJECT_ID = B.PROJECT_ID\n" +
            "   AND UB.BRANCH_ID = B.BRANCH_ID\n" +
            "   AND GU.USER_NAME = U.LOGIN_NAME\n" +
            "   AND UB.USER_ID = U.USER_ID\n" +
            "   AND UB.USER_ID = :userId")
            .setParameter("userId", userId).findList();
        return list;
    }

    public List<SqlRow> qryWorkingSpaceByUser(String userName) {
        List<SqlRow> list = Ebean.createSqlQuery("SELECT G.GROUP_NAME,P.PROJECT_NAME,B.BRANCH_ID,B.BRANCH_NAME,B.BRANCH_DESC\n" +
            "  FROM AD_USER_BRANCH   UB,\n" +
            "       AD_GROUP_USER    GU,\n" +
            "       AD_BRANCH        B,\n" +
            "       AD_PROJECT      P,\n" +
            "       AD_GROUP         G,\n" +
            "       AD_USER          U\n" +
            " WHERE UB.STATE = 1 \n" +
            "   AND GU.STATE = 1\n" +
            "   AND B.STATE = 1\n" +
            "   AND P.STATE = 1\n" +
            "   AND G.STATE = 1\n" +
            "   AND G.GROUP_ID = GU.GROUP_ID\n" +
            "   AND G.GROUP_ID = P.GROUP_ID\n" +
            "   AND P.PROJECT_ID = B.PROJECT_ID\n" +
            "   AND UB.BRANCH_ID = B.BRANCH_ID\n" +
            "   AND GU.USER_NAME = U.LOGIN_NAME\n" +
            "   AND UB.USER_ID = U.USER_ID\n" +
            "   AND UB.USER_NAME = :userName")
            .setParameter("userName", userName).findList();
        return list;
    }

    public void addBranchToWorkSpace(AdBranch branch, Long userId, String username) {
        AdUser user = new AdUser();
        user.setUserId(userId);
        Date now = new Date();
        AdUserBranch userBranch = new AdUserBranch();
        userBranch.setAdBranch(branch);
        userBranch.setCreateDate(now);
        userBranch.setAdUser(user);
        userBranch.setDoneDate(now);
        userBranch.setState(1);
        userBranch.setUserName(username);
        userBranch.save();
    }

    public void updateNoRoleData(String username) {
        Ebean.createSqlUpdate("UPDATE AD_USER_BRANCH UB\n" +
            "   SET UB.STATE = 0, UB.DONE_DATE = :doneDate\n" +
            " WHERE UB.STATE = 1\n" +
            "   AND UB.USER_NAME = :userName\n" +
            "   AND NOT EXISTS\n" +
            " (SELECT 1\n" +
            "          FROM AD_GROUP_USER GU, AD_BRANCH B, AD_PROJECT P, AD_GROUP G\n" +
            "         WHERE UB.STATE = 1\n" +
            "           AND GU.STATE = 1\n" +
            "           AND B.STATE = 1\n" +
            "           AND P.STATE = 1\n" +
            "           AND G.STATE = 1\n" +
            "           AND GU.GROUP_ID = G.GROUP_ID(+)\n" +
            "           AND G.GROUP_ID = P.GROUP_ID(+)\n" +
            "           AND P.PROJECT_ID = B.PROJECT_ID\n" +
            "           AND GU.USER_NAME = :userName\n" +
            "           AND UB.BRANCH_ID = B.BRANCH_ID)\n")
            .setParameter("doneDate", new Date()).setParameter("userName", username).execute();
    }

    public int qryNumByBranchIdAndAdminUserName(Long branchId, String userName) {
        return new QAdUserBranch().state.eq(1).adBranch.state.eq(1).userName.eq(userName).adBranch.branchId.eq(branchId).findRowCount();
    }

    // 查询存在工作台的流水数量
    public int qryNumByBranchIdAndUserName(Long branchId, String userName) {
        int num = 0;
        List<SqlRow> list = Ebean.createSqlQuery("SELECT COUNT(*) AS NUM\n" +
            "  FROM AD_USER_BRANCH UB\n" +
            " WHERE UB.STATE = 1\n" +
            "   AND UB.USER_NAME = :userName\n" +
            "   AND UB.BRANCH_ID = :branchId\n" +
            "   AND NOT EXISTS\n" +
            " (SELECT 1\n" +
            "          FROM AD_GROUP_USER GU, AD_BRANCH B, AD_PROJECT P, AD_GROUP G\n" +
            "         WHERE UB.STATE = 1\n" +
            "           AND GU.STATE = 1\n" +
            "           AND B.STATE = 1\n" +
            "           AND P.STATE = 1\n" +
            "           AND G.STATE = 1\n" +
            "           AND GU.GROUP_ID = G.GROUP_ID(+)\n" +
            "           AND G.GROUP_ID = P.GROUP_ID(+)\n" +
            "           AND P.PROJECT_ID = B.PROJECT_ID\n" +
            "           AND GU.USER_NAME = :userName\n" +
            "           AND UB.BRANCH_ID = B.BRANCH_ID)")
            .setParameter("branchId", branchId).setParameter("userName", userName).findList();
        if (list != null && list.size() > 0) {
            num = list.get(0).getInteger("NUM");
        }
        return num;
    }

    public void update(List<AdUserBranch> userBranchList) {
        userBranchList.forEach(Model::update);
    }

    public List<AdUserBranch> qryWorkingSpaceByAdminUser(Long userId) {
        return new QAdUserBranch().state.eq(1).adBranch.state.eq(1).adUser.userId.eq(userId).orderBy("CREATE_DATE ASC").findList();
    }
}
