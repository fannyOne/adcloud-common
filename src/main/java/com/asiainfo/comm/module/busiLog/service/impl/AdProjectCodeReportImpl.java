package com.asiainfo.comm.module.busiLog.service.impl;

import com.asiainfo.comm.module.busiLog.dao.impl.AdProjectCodeReportDAO;
import com.asiainfo.comm.module.models.AdProjectCodeReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by weif on 2016/12/30.
 */
@Component
public class AdProjectCodeReportImpl {
    @Autowired
    AdProjectCodeReportDAO adProjectCodeReportDAO;

    public void saveAll(List<AdProjectCodeReport> adProjectCodeReports) {
        adProjectCodeReportDAO.saveAll(adProjectCodeReports);
    }

}
