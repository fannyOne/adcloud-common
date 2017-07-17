package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.common.pojo.pojoExt.AdAllGroupPojoExt;
import com.asiainfo.comm.module.models.AdGroup;
import com.asiainfo.comm.module.models.functionModels.AdGroupAndProject;
import com.asiainfo.comm.module.models.functionModels.query.QAdGroupAndProject;
import com.asiainfo.comm.module.models.query.QAdGroup;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by weif on 2016/6/15.
 */
@Component
public class AdGroupDAO {
    private String qryGroupSignUser = "SELECT G.*, UR.STATE AS UR_STATE\n" +
        "  FROM AD_GROUP G, AD_GROUP_USER GU, AD_USER_DATA_RELATE UR, AD_USER U\n" +
        " WHERE G.GROUP_ID = GU.GROUP_ID\n" +
        "   AND U.USER_ID = :userId\n" +
        "   AND GU.USER_NAME = U.LOGIN_NAME\n" +
        "   AND G.GROUP_ID = UR.FOREIGN_ID(+)\n" +
        "   AND UR.USER_ID(+) = :userId\n" +
        "   AND UR.DATA_TYPE(+) = :dataType\n" +
        "   AND UR.REPORT_TYPE(+) = :reportType\n" +
        "   AND UR.STATE(+) = 1\n" +
        "   AND G.STATE = 1\n" +
        "   AND GU.STATE = 1\n" +
        "   AND U.STATE = 1\n" +
        "   ORDER BY G.GROUP_NAME ASC";
    private String qryGroupSignAdmin = "SELECT G.*, UR.STATE AS UR_STATE\n" +
        "  FROM AD_GROUP G, AD_USER_DATA_RELATE UR\n" +
        " WHERE G.GROUP_ID = UR.FOREIGN_ID(+)\n" +
        "   AND UR.USER_ID(+) = :userId\n" +
        "   AND UR.DATA_TYPE(+) = :dataType\n" +
        "   AND UR.REPORT_TYPE(+) = :reportType\n" +
        "   AND UR.STATE(+) = 1\n" +
        "   AND G.STATE = 1\n" +
        "   ORDER BY G.GROUP_NAME ASC";

    private String qrySonarGroupSignAdmin = "SELECT G.GROUP_ID, G.GROUP_NAME, UR.STATE AS UR_STATE\n" +
        "  FROM AD_GROUP G, AD_USER_DATA_RELATE UR, AD_TREE_DATA T\n" +
        " WHERE G.GROUP_ID = CAST(T.TREE_CODE AS NUMBER)\n" +
        "   AND G.GROUP_ID = UR.FOREIGN_ID(+)\n" +
        "   AND UR.DATA_TYPE(+) = :dataType\n" +
        "   AND UR.REPORT_TYPE(+) = :reportType\n" +
        "   AND UR.USER_ID(+) = :userId\n" +
        "   AND T.TREE_TYPE = 2\n" +
        "   AND G.STATE = 1\n" +
        "   AND T.STATE = 1\n" +
        "   AND UR.STATE(+) = 1" +
        "   ORDER BY G.GROUP_NAME ASC";

    public AdGroup saveAdGroup(AdGroup adGroup) {
        if (adGroup != null) {
            Ebean.save(adGroup);
        }
        return adGroup;
    }

    public AdGroup qryAdGroupByname(String name) {
        List<AdGroup> adGroupList = new QAdGroup().state.eq(1).groupName.eq(name).findList();
        if (adGroupList != null && adGroupList.size() > 0) {
            return adGroupList.get(0);
        } else {
            return null;
        }
    }

    public AdGroup qryAdGroupById(int id) {
        AdGroup adGroup = new QAdGroup().state.eq(1).groupId.eq(id).findUnique();
        return adGroup;
    }

    public List<AdGroup> qryAllGroup() {
        return new QAdGroup().state.eq(1).findList();
    }

    public List<AdGroupAndProject> qryAdGroupAndProject() {
        return new QAdGroupAndProject().state.eq(1).orderBy(" GROUP_NAME ASC").findList();
    }

    public AdGroupAndProject qryAdGroupAndProjectId(long id) {
        return new QAdGroupAndProject().state.eq(1).groupId.eq(id).findUnique();
    }


    public List<SqlRow> qryIndexGroup() {
        String sql = "SELECT GRO.GROUP_ID,\n" +
            "       GRO.GROUP_NAME,\n" +
            "       GRO.GROUP_DESC,\n" +
            "       GRO.GROUP_STYLE,\n" +
            "       GRO.IMAGE_ICON,\n" +
            "       COUNT(PRO.PROJECT_ID) AS PRO_NUM,\n" +
            "       COUNT(PIPELINE_STATE_FAILED.PIPELINE_ID) AS FAIL_NUM,\n" +
            "       COUNT(PIPELINE_STATE_RUN.PIPELINE_ID) AS RUN_NUM,\n" +
            "       COUNT(PIPELINE_STATE_SUC.PIPELINE_ID) AS SUC_NUM\n" +
            "  FROM AD_GROUP          GRO,\n" +
            "       AD_PROJECT       PRO,\n" +
            "       AD_PIPELINE_STATE PIPELINE_STATE_FAILED,\n" +
            "       AD_PIPELINE_STATE PIPELINE_STATE_RUN,\n" +
            "       AD_PIPELINE_STATE PIPELINE_STATE_SUC\n" +
            " WHERE GRO.GROUP_ID = PRO.GROUP_ID(+)\n" +
            "   AND PIPELINE_STATE_FAILED.LAST_BUILD_RESULT(+) = 3\n" +
            "   AND PIPELINE_STATE_FAILED.PROJECT_ID(+) = PRO.PROJECT_ID\n" +
            "   AND PIPELINE_STATE_FAILED.STATE(+) = 1\n" +
            "   AND PIPELINE_STATE_RUN.BRANCH_STATE(+) = 2\n" +
            "   AND PIPELINE_STATE_RUN.PROJECT_ID(+) = PRO.PROJECT_ID\n" +
            "   AND PIPELINE_STATE_RUN.STATE(+) = 1\n" +
            "   AND PIPELINE_STATE_SUC.LAST_BUILD_RESULT(+) = 2\n" +
            "   AND PIPELINE_STATE_SUC.PROJECT_ID(+) = PRO.PROJECT_ID\n" +
            "   AND PIPELINE_STATE_SUC.STATE(+) = 1\n" +
            "   AND GRO.STATE = 1\n" +
            "   AND PRO.STATE(+) = 1\n" +
            " GROUP BY GRO.GROUP_ID,\n" +
            "          GRO.GROUP_NAME,\n" +
            "          GRO.GROUP_DESC,\n" +
            "          GRO.GROUP_STYLE,\n" +
            "          GRO.IMAGE_ICON\n" +
            " ORDER BY GROUP_STYLE ASC, GROUP_NAME ASC";
        List<SqlRow> rows = Ebean.createSqlQuery(sql).findList();
        return rows;
    }

    public long qryCountAllGroup() {
        return new QAdGroup().state.eq(1).findRowCount();
    }

    public long countGroupCreateDate(Date date) {
        return new QAdGroup().state.eq(1).createDate.after(date).findRowCount();
    }

    public int qryRowById(long value) {
        return new QAdGroup().groupId.eq(value).state.eq(1).findRowCount();
    }

    public List<SqlRow> qryGroupSignUser(int dataType, Long userId, Integer reportType) {
        return Ebean.createSqlQuery(qryGroupSignUser).setParameter("dataType", dataType).setParameter("userId", userId).setParameter("reportType", reportType).findList();
    }

    public List<SqlRow> qryGroupSignAdmin(int dataType, Long userId, Integer reportType) {
        return Ebean.createSqlQuery(qryGroupSignAdmin).setParameter("dataType", dataType).setParameter("userId", userId).setParameter("reportType", reportType).findList();
    }

    public List<SqlRow> qryGroupByIds(String groupIds) {
        String sql = "select group_id,group_name from ad_group where state=1 and group_id in (" + groupIds + ") ";
        SqlQuery sqlQuery = createSqlQuery(sql);
        return sqlQuery.findList();
    }

    public List<SqlRow> qrySonarGroup(Long userId, Integer dataType, Integer reportType) {
        return Ebean.createSqlQuery(qrySonarGroupSignAdmin).setParameter("userId", userId).setParameter("dataType", dataType).setParameter("reportType", reportType).findList();
    }


    /**
     * 查询所有项目
     *
     * @return 查询到的结果
     */
    public List<AdAllGroupPojoExt> qryAllGroupName() {
        //查询状态是1有效的所有项目
        List<AdGroup> adGroups = new QAdGroup().state.eq(1).orderBy(" GROUP_NAME ASC").findList();
        List<AdAllGroupPojoExt> adAllGroups = new ArrayList<AdAllGroupPojoExt>();
        //遍历项目
        if (adGroups != null) {
            for (AdGroup adGroup : adGroups) {
                AdAllGroupPojoExt adAllGroup = new AdAllGroupPojoExt();
                adAllGroup.setGroupId(adGroup.getGroupId());
                adAllGroup.setGroupName(adGroup.getGroupName());
                adAllGroups.add(adAllGroup);
            }
        }
        return adAllGroups;
    }

}
