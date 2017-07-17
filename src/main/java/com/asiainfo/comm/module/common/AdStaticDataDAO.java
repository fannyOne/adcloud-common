package com.asiainfo.comm.module.common;

import com.asiainfo.comm.module.models.AdStaticData;
import com.asiainfo.comm.module.models.query.QAdStaticData;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weif on 2016/6/15.
 */
@Component("AdStaticDataDao")
public class AdStaticDataDAO {

    /**
     * FIXME:List数据作为value写入缓存时,数据较多时容易大小越界,请确认数据结构的正确性
     * guojian
     *
     * @param codeType
     * @return
     */
    public List<AdStaticData> qryByCodeType(String codeType) {
        List<AdStaticData> list = new QAdStaticData().codeType.eq(codeType).orderBy(" SORT_ID ASC").findList();
        if (list != null && list.size() > 0) {
            return list;
        } else {
            return null;
        }
    }

    public void save(AdStaticData adStaticData) {
        adStaticData.save();
    }

    /**
     * FIXME:更新的时候删除缓存
     * guojian
     *
     * @param adStaticData
     */
    public void update(AdStaticData adStaticData) {
        String sql = "UPDATE AD_STATIC_DATA SET CODE_VALUE =:codeValue WHERE CODE_TYPE =:codeType";
        SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
        sqlUpdate.setParameter("codeValue", adStaticData.getCodeValue());
        sqlUpdate.setParameter("codeType", adStaticData.getCodeType());
        int updateNum = Ebean.execute(sqlUpdate);
        System.out.println("更新IP条数：" + updateNum);
    }

    /**
     * FIXME:查询时,从缓存加载,如果缓存无数据,则执行代码,然后添加缓存
     * guojian
     *
     * @param codeType
     * @param codeValue
     * @return
     */
    public AdStaticData qryStaticDataByCodeValue(String codeType, String codeValue) {
        return new QAdStaticData().codeType.eq(codeType).codeValue.eq(codeValue).findUnique();
    }


    public Map<String, String> qryStaticDatas(String codeType) {
        Map<String, String> staticdataMap = new HashMap<String, String>();
        List<AdStaticData> adStaticDatas = new QAdStaticData().codeType.eq(codeType).findList();
        if (adStaticDatas != null && adStaticDatas.size() > 0) {
            for (AdStaticData adStaticData : adStaticDatas) {
                staticdataMap.put(adStaticData.getCodeValue(), adStaticData.getCodeName());
            }
        }
        return staticdataMap;
    }

    public Map<String, String> qryStaticDataAlias(String codeType) {
        Map<String, String> staticdataMap = new HashMap<String, String>();
        List<AdStaticData> adStaticDatas = new QAdStaticData().codeType.eq(codeType).findList();
        if (adStaticDatas != null) {
            for (AdStaticData adStaticData : adStaticDatas) {
                staticdataMap.put(adStaticData.getCodeValue(), adStaticData.getCodeTypeAlias());
            }
        }
        return staticdataMap;
    }
}
