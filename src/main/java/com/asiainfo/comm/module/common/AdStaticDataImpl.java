package com.asiainfo.comm.module.common;

import com.asiainfo.comm.module.models.AdStaticData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by YangRY on 2016/6/30 0030.
 */
@Component
public class AdStaticDataImpl {
    @Autowired
    AdStaticDataDAO bsStaticDataDAO;

    public List<AdStaticData> qryByCodeType(String codeType) {
        return bsStaticDataDAO.qryByCodeType(codeType);
    }

    public AdStaticData qryStaticDataByCodeValue(String codeType, String codeValue) {
        return bsStaticDataDAO.qryStaticDataByCodeValue(codeType, codeValue);
    }

    public void save(AdStaticData adStaticData) {
        bsStaticDataDAO.save(adStaticData);
    }

    public Map<String, String> qryStaticDatas(String codeType) {
        return bsStaticDataDAO.qryStaticDatas(codeType);
    }
}
