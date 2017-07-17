package com.asiainfo.comm.module.deploy.service.impl;

import com.asiainfo.comm.module.deploy.dao.impl.SystemDeployLogDAO;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import com.avaje.ebean.TxType;
import com.avaje.ebean.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by GUOJIAN on 7/26/16.
 */
@Component("SystemDeployLogImpl")
public class SystemDeployLogImpl {
    @Value("${pagesize}")
    int pageSize;
    @Autowired
    private SystemDeployLogDAO systemDeployLogDAO;

    /**
     * 根据system id查询当前系统的发布历史日志
     *
     * @param id
     * @return
     */
    public List<AdSystemDeployLog> getLogsBySystemId(Long id) {
        return systemDeployLogDAO.getLogsBySystemId(id);

    }

    /**
     * 根据system id写入当前系统的发布历史日志
     *
     * @param log
     */
    @Transactional(type = TxType.REQUIRES_NEW)
    public void addLogsBySystemId(AdSystemDeployLog log) {
        systemDeployLogDAO.addLogsBySystemId(log);
    }

    /**
     * 根据system id写入当前系统的发布历史日志
     *
     * @param log
     */
    public long saveAndReturn(AdSystemDeployLog log) {
        return systemDeployLogDAO.saveAndReturn(log);
    }

    public Map<String, Object> qrySystemDeployLogs(AdSystemDeployLog adSystemDeployLog, int pageNum, long opId) {
        Map<String, Object> adSystemDeployLogList = systemDeployLogDAO.qrySystemDeployLogs(adSystemDeployLog, pageNum, pageSize, opId);

        return adSystemDeployLogList;
    }

    public AdSystemDeployLog qryById(Long logId) {
        return systemDeployLogDAO.qryById(logId);
    }

    public List<AdSystemDeployLog> qryInvalidPlan() {
        return systemDeployLogDAO.qryInvalidPlan();
    }

    public List<AdSystemDeployLog> qryNoDealPlan() {
        return systemDeployLogDAO.noDealPlan();
    }

    public boolean changeState(AdSystemDeployLog log, int state, String jobToken) {
        return systemDeployLogDAO.changeState(log, state, jobToken);
    }
}
