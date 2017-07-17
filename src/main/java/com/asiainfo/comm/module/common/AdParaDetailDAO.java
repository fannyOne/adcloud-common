package com.asiainfo.comm.module.common;

import com.asiainfo.comm.module.models.AdParaDetail;
import com.asiainfo.comm.module.models.query.QAdParaDetail;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by YangRY
 * on 2016/6/20 0020.
 */
@Component
public class AdParaDetailDAO {

    public List<AdParaDetail> qryByCode(String code) {
        List<AdParaDetail> params = new QAdParaDetail().paraCode.eq(code).state.eq("U").findList();
        return params;
    }

    public List<AdParaDetail> qryByDetails(String regionId, String paraType, String paraCode) {
        List<AdParaDetail> params = new QAdParaDetail().regionId.eq(regionId).paraType.eq(paraType).paraCode.eq(paraCode).state.eq("U").findList();
        return params;
    }

    public AdParaDetail qryByParams(String regionId, String paraType, String paraCode) {
        return new QAdParaDetail().state.eq("U").regionId.eq(regionId).paraType.eq(paraType).paraCode.eq(paraCode).findUnique();
    }

    public int qryRowByDetails(String regionId, String paraType, String paraCode) {
        return new QAdParaDetail().regionId.eq(regionId).paraType.eq(paraType)
            .paraCode.eq(paraCode).state.eq("U").findRowCount();
    }

    public int qryRowByDetails(String regionId, String paraType, String paraCode, String para1) {
        return new QAdParaDetail().regionId.eq(regionId).paraType.eq(paraType)
            .paraCode.eq(paraCode).para1.eq(para1).state.eq("U").findRowCount();
    }

    public List<AdParaDetail> qryListByDetails(String regionId, String paraType, String paraCode, String para1) {
        return new QAdParaDetail().regionId.eq(regionId).paraType.eq(paraType)
            .paraCode.eq(paraCode).para1.eq(para1).state.eq("U").findList();
    }

    public int qryRowByDetails(String regionId, String paraType, String paraCode, String para1, String para2) {
        return new QAdParaDetail().regionId.eq(regionId).paraType.eq(paraType).para2.eq(para2)
            .paraCode.eq(paraCode).para1.eq(para1).state.eq("U").findRowCount();
    }

    public List<AdParaDetail> qryListByDetails(String regionId, String paraType, String paraCode, String para1, String para2) {
        return new QAdParaDetail().regionId.eq(regionId).paraType.eq(paraType).para2.eq(para2)
            .paraCode.eq(paraCode).para1.eq(para1).state.eq("U").findList();
    }

    public List<AdParaDetail> qryListByParaType(String regionId, String paraType) {
        return new QAdParaDetail().regionId.eq(regionId).paraType.eq(paraType).state.eq("U").findList();
    }
}
