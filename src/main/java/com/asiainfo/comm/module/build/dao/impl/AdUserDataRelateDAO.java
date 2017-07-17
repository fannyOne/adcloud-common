package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdUserDataRelate;
import com.asiainfo.comm.module.models.functionModels.AdUserDataRelateSimple;
import com.asiainfo.comm.module.models.functionModels.query.QAdUserDataRelateSimple;
import com.asiainfo.comm.module.models.query.QAdUserDataRelate;
import com.avaje.ebean.Ebean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by YangRY on 2016/11/9.
 */
@Component
public class AdUserDataRelateDAO {
    private String deleteSql = "UPDATE AD_USER_DATA_RELATE T\n" +
        "   SET T.STATE = 0,\n" +
        "   T.DONE_DATE = SYSDATE\n" +
        "   WHERE T.USER_ID = :userId\n" +
        "   AND T.DATA_TYPE = :dataType\n" +
        "   AND T.REPORT_TYPE = :reportType\n";

    public List<AdUserDataRelateSimple> qryByParaNoStateSimple(Long userId, Integer dataType, Integer reportType) {
        return new QAdUserDataRelateSimple().userId.eq(userId).dataType.eq(dataType).reportType.eq(reportType).findList();
    }

    public List<AdUserDataRelate> qryByPara(Long userId, Integer dataType, Integer reportType) {
        return new QAdUserDataRelate().state.eq(1).adUser.userId.eq(userId).dataType.eq(dataType).reportType.eq(reportType).findList();
    }

    public void save(List<AdUserDataRelate> relateList) {
        Ebean.saveAll(relateList);
    }

    public void delete(Long userId, Integer dataType, Integer reportType) {
        Ebean.createSqlUpdate(deleteSql).setParameter("userId", userId).setParameter("dataType", dataType)
            .setParameter("reportType", reportType).execute();
    }

    public void saveSimple(List<AdUserDataRelateSimple> updateList) {
        Ebean.saveAll(updateList);
    }
}
