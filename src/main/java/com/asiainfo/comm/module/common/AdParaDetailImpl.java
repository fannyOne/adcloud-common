package com.asiainfo.comm.module.common;

import com.asiainfo.comm.module.models.AdParaDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/19 0019.
 */
@Component
public class AdParaDetailImpl {
    @Autowired
    AdParaDetailDAO bsParaDetailDAO;

    public AdParaDetail qryByDetails(String x, String s, String s1) {
        List<AdParaDetail> adParaDetailList = bsParaDetailDAO.qryByDetails(x, s, s1);
        if (adParaDetailList != null && adParaDetailList.size() > 0) {
            return adParaDetailList.get(0);
        } else {
            return null;
        }
    }
}