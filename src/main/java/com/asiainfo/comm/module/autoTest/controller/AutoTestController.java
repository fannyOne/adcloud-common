package com.asiainfo.comm.module.autoTest.controller;

import com.asiainfo.comm.common.pojo.pojoExt.AutoTestLogPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.AutoTestLogPojo;
import com.asiainfo.comm.module.autoTest.service.impl.AdAutoTestLogImpl;
import com.asiainfo.comm.module.autoTest.service.impl.AdSeqTestRelateImpl;
import com.asiainfo.comm.module.build.service.impl.AdStageImpl;
import com.asiainfo.comm.module.build.service.impl.AdStageLogDtlImpl;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.avaje.ebean.PagedList;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by YangRy on 2016/7/25.
 */
@RestController
@lombok.extern.slf4j.Slf4j
@RequestMapping(value = "/autoTest")
/**
 * 自动化测试相关Controller
 */
public class AutoTestController {
    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;
    @Autowired
    private AdStageImpl stageImpl;
    @Autowired
    private AdStaticDataImpl staticDataImpl;
    @Autowired
    private AdAutoTestLogImpl autoTestLogImpl;
    @Autowired
    private AdSeqTestRelateImpl seqTestRelateImpl;
    @Autowired
    private AdStageLogDtlImpl stageLogDtlImpl;

    //测试管理平台返回数据处理
    @RequestMapping(value = "/changeStepByAutoTest", method = RequestMethod.GET, produces = "application/json")
    public String changeStepByAutoTest(@RequestParam Map map) throws SQLException, ParseException {
        List<AdStaticData> staticDataList = staticDataImpl.qryByCodeType("AUTO_TEST_DETAIL_URL");
        String testUrl;
        AdAutoTestLog autoTestLog = new AdAutoTestLog();
        if (staticDataList != null && staticDataList.size() > 0) {
            testUrl = staticDataList.get(0).getCodeValue();
        } else {
            testUrl = "http://20.26.17.182:9001/searchADCloudTaskResult.do";
        }
        String seqId = (String) map.get("seqId");
        long totalNum = map.get("totalNum") != null ? Long.valueOf((String) map.get("totalNum")) : 0;
        long sucNum = map.get("sucNum") != null ? Long.valueOf((String) map.get("sucNum")) : 0;
        long failNum = map.get("failNum") != null ? Long.valueOf((String) map.get("failNum")) : 0;
        String sucPre = sucNum * 100 / totalNum + "%";
        long seq = -1;
        long stagId = 0;
        Long testId;
        long stageId;
        String testLog = "";
        String autoType = null;
        if (seqId != null && seqId.contains("_")) {
            String[] split = seqId.split("_");
            if (split.length >= 3) {
                seq = Long.parseLong(split[0]);
                stagId = Long.parseLong(split[1]);
                autoType = split[2];
            }
        }
        // 更改状态
        AdStage adStage = stageImpl.qryById(stagId);
        if (adStage != null) {
            if (failNum > 0) {
                log.error("---------------------置为失败：failNum:\n" + failNum + "\n" + "sucNum:" + sucNum);
                stageImpl.updateState(adStage, 3);
            } else {
                log.error("---------------------置为成功：failNum:\n" + failNum + "\n" + "sucNum:" + sucNum);
                stageImpl.updateState(adStage, 2);
            }
        }
        // 收集日志信息
        try {
            List<AdSeqTestRelate> relates = seqTestRelateImpl.qryBySeqAndType(seq, autoType);
            if (CollectionUtils.isEmpty(relates)) {
                for (AdSeqTestRelate relate : relates) {
                    Date date = new Date();
                    autoTestLog.setCreateDate(date);
                    autoTestLog.setBeginDate(relate.getCreateDate());
                    autoTestLog.setEndDate(date);
                    autoTestLog.setSeqId(seq);
                    if (adStage != null) {
                        autoTestLog.setAdBranch(adStage.getAdBranch());
                    }
                    stageId = relate.getAdStage().getStageId();
                    testId = relate.getTestId();
                    autoTestLog.setAdStage(adStage);
                    autoTestLog.setState(1);
                    autoTestLog.setSucNum(sucNum);
                    autoTestLog.setFailNum(failNum);
                    autoTestLog.setSucPre(sucPre);
                    autoTestLog.setTotalNum(totalNum);
                    autoTestLog.setTestId(testId);
                    if (relate.getTestType().equals("ALL")) {
                        autoTestLog.setAutoType(1);
                        testLog += "<a href=\"" + testUrl + "?taskId="
                            + testId + "\" target=\"_blank\">查看详情</a>";
                        autoTestLog.setTestLog(testLog);
                        autoTestLogImpl.save(autoTestLog);
                    } else {
                        autoTestLog.setAutoType(2);
                        AdStageLogDtl stageLog;
                        testLog += "<a href=\"" + testUrl + "?taskId="
                            + testId + "\" target=\"_blank\">查看详情</a>";
                        autoTestLog.setTestLog(testLog);
                        autoTestLogImpl.save(autoTestLog);
                        stageLog = stageLogDtlImpl.qryBySeqStage(seq, stageId);
                        if (stageLog != null) {
                            stageLog.setFailLog(testLog);
                            stageLogDtlImpl.update(stageLog);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
        return "aaa";
    }

    @RequestMapping(value = "/qryAutoTestLog", produces = "application/json")
    public AutoTestLogPojo qryAutoTestLog(@RequestParam Map map) throws IOException {
        AutoTestLogPojo poj = new AutoTestLogPojo();
        String branchId = (String) map.get("branchId");
        String page = (String) map.get("page");
        String pageSize = (String) map.get("pageSize");
        if (StringUtils.isNotEmpty(branchId)) {

        /* 资源隔离，权限验证 */
            if (!userRoleRelImpl.verifyPurview("branchId", Long.parseLong(branchId))) {
                return null;
            }
        /* 资源隔离，权限验证 Over */

            PagedList<AdAutoTestLog> autoLogPage = autoTestLogImpl
                .qryAutoTestLogByBranchId(Long.parseLong(branchId), StringUtils.isNotEmpty(page) ?
                    Integer.parseInt(page) - 1 : 0, StringUtils.isNotEmpty(pageSize) ?
                    Integer.parseInt(pageSize) : 10, 1);
            if (autoLogPage != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                List<AdAutoTestLog> autoLogList = autoLogPage.getList();
                poj.setTotalSize(autoLogPage.getTotalRowCount());
                if (autoLogList != null && autoLogList.size() > 0) {
                    List<AutoTestLogPojoExt> pojExtList = new ArrayList<>();
                    for (AdAutoTestLog log : autoLogList) {
                        AutoTestLogPojoExt pojExt = new AutoTestLogPojoExt();
                        pojExt.setState(log.getFailNum() > 0 ? "warning" : "success");
                        if (log.getAdBranch() != null) {
                            pojExt.setBranchId(log.getAdBranch().getBranchId());
                        }
                        pojExt.setEndDate(sdf.format(log.getEndDate()));
                        pojExt.setLogInfo(log.getTestLog());
                        pojExt.setSeqId(log.getSeqId());
                        pojExt.setTestId(log.getTestId());
                        pojExt.setSucNum(log.getSucNum());
                        pojExt.setFailNum(log.getFailNum());
                        pojExt.setTotalNum(log.getTotalNum());
                        pojExt.setSucPre(log.getSucPre());
                        if (log.getAdStage() != null) {
                            pojExt.setStageId(log.getAdStage().getStageId());
                        }
                        pojExt.setStartDate(sdf.format(log.getBeginDate()));
                        pojExtList.add(pojExt);
                    }
                    poj.setLogs(pojExtList);
                }
            }
        }
        return poj;
    }
}



