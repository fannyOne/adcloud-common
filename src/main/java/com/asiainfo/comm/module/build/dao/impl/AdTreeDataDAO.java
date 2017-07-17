package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdTreeData;
import com.asiainfo.comm.module.models.query.QAdTreeData;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by YangRY on 2016/11/9.
 */
@Component
public class AdTreeDataDAO {
    String qryByIdListSql = "SELECT T.*,\n" +
        "       P.ID        AS P_ID,\n" +
        "       P.TREE_CODE AS P_TREE_CODE,\n" +
        "       P.TREE_PARA AS P_TREE_PARA,\n" +
        "       P.TREE_NAME AS P_TREE_NAME\n" +
        "  FROM AD_TREE_DATA T, AD_TREE_DATA P\n" +
        " WHERE T.STATE = 1\n" +
        "   AND P.STATE(+) = 1\n" +
        "   AND T.ID IN (:idList)\n" +
        "   AND P.ID(+) = T.PARENT_ID" +
        "   ORDER BY P.ID ASC,T.ID ASC";

    public List<AdTreeData> qryByTreeType(Integer treeType) {
        return new QAdTreeData().state.eq(1).treeType.eq(treeType).parentId.eq(0).findList();
    }

    public AdTreeData qryByTreePara(Long treeCode) {
        List<AdTreeData> list =
            new QAdTreeData().treeType.eq(2).treeCode.eq(treeCode.toString()).findList();
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public List<AdTreeData> qryByIds(String idListStr) {
        boolean first = true;
        if (idListStr == null || idListStr.equals("")) {
            return null;
        } else {
            String[] idList = idListStr.split(",");
            QAdTreeData treeDataQry = new QAdTreeData().state.eq(1).or();
            for (String id : idList) {
                if (!first) {
                    treeDataQry.id.eq(Long.parseLong(id));
                } else {
                    treeDataQry.id.eq(Long.parseLong(id));
                    first = false;
                }
            }
            return treeDataQry.endOr().orderBy(" PARENT_ID ASC ").orderBy(" ID ASC ").findList();
        }
    }

    public List<SqlRow> qryTreeDataByIdList(String idList) {
        return Ebean.createSqlQuery(qryByIdListSql.replace(":idList", idList)).findList();
    }
}
