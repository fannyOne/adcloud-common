package com.asiainfo.comm.module.busiLog.dao.impl;

import com.asiainfo.comm.module.models.AdBusiLog;
import com.asiainfo.comm.module.models.query.QAdBusiLog;
import com.avaje.ebean.Ebean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhenghp on 2016/8/8.
 */
@Component
public class AdBusiLogDAO {
    public AdBusiLog save(AdBusiLog adBusiLog) {
        if (adBusiLog != null) {
            Ebean.save(adBusiLog);
        }
        return adBusiLog;
    }

    public List<AdBusiLog> qryByBusiCode(Long busiCode) {
        return new QAdBusiLog().busiCode.eq(busiCode).orderBy(" CREATE_DATE DESC").findList();
    }
}
