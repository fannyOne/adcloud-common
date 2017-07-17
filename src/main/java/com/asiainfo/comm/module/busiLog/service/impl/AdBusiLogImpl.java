package com.asiainfo.comm.module.busiLog.service.impl;

import com.asiainfo.comm.common.enums.BusiCode;
import com.asiainfo.comm.module.busiLog.dao.impl.AdBusiLogDAO;
import com.asiainfo.comm.module.models.AdBusiLog;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class AdBusiLogImpl {
    @Autowired
    AdBusiLogDAO adBusiLogDAO;

    public AdBusiLog save(AdBusiLog adBusiLog) {
        return adBusiLogDAO.save(adBusiLog);
    }

    public AdBusiLog save(BusiCode code, Long opId, Long projectId, String message) {
        AdBusiLog adBusiLog = new AdBusiLog();
        adBusiLog.setBusiCode(code.getCode());
        String detail = code.getDescription();
        if (!Strings.isNullOrEmpty(message)) {
            detail += message;
        }
        if (detail.length() > 1000) {
            detail = detail.substring(0, 1000);
        }
        adBusiLog.setBusiDetail(detail);
        adBusiLog.setCreateDate(new Date());
        adBusiLog.setOpId(opId);
        adBusiLog.setProjectId(projectId);
        return adBusiLogDAO.save(adBusiLog);
    }

    public List<AdBusiLog> qryByBusiCode(Long busiCode) {
        return adBusiLogDAO.qryByBusiCode(busiCode);
    }
}
