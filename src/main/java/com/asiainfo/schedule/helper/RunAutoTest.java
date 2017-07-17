package com.asiainfo.schedule.helper;

import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.comm.module.autoTest.service.impl.AdAutoTestLogImpl;
import com.asiainfo.comm.module.autoTest.service.impl.AdSeqTestRelateImpl;
import com.asiainfo.comm.module.build.service.impl.AdStageImpl;
import com.asiainfo.comm.module.build.service.impl.AdStageLogDtlImpl;
import com.asiainfo.comm.module.common.AdParaDetailImpl;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.webService.TestPlatform;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.asiainfo.util.CommConstants.BuildConstants.AD_CLOUD_IP;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */
@lombok.extern.slf4j.Slf4j
public class RunAutoTest implements Runnable {
    AdStaticDataImpl staticDataImpl;
    AdAutoTestLogImpl autoTestLogImpl;
    AdStageImpl adStageImpl;
    AdParaDetailImpl bsParaDetailImpl;
    AdSeqTestRelateImpl seqTestRelateImpl;
    AdStageLogDtlImpl adStageLogDtlImpl;
    private String url;
    private AdBranch adBranch;
    private AdRmpBranchRelate rmpBranchRelate;
    private AdStage adStage;

    public RunAutoTest(AdBranch adBranch, AdStage adStage, AdRmpBranchRelate rmpBranchRelate,
                       AdStaticDataImpl staticDataImpl, AdAutoTestLogImpl autoTestLogImpl,
                       AdStageImpl stageImpl, AdParaDetailImpl paraDetailImpl,
                       AdSeqTestRelateImpl seqTestRelateImpl, AdStageLogDtlImpl adStageLogDtlImpl) {
        this.adBranch = adBranch;
        this.adStage = adStage;
        this.rmpBranchRelate = rmpBranchRelate;
        this.staticDataImpl = staticDataImpl;
        this.autoTestLogImpl = autoTestLogImpl;
        this.adStageImpl = stageImpl;
        this.bsParaDetailImpl = paraDetailImpl;
        this.seqTestRelateImpl = seqTestRelateImpl;
        this.adStageLogDtlImpl = adStageLogDtlImpl;
    }

    public RunAutoTest(AdBranch adBranch, AdStage adStage,
                       AdStaticDataImpl staticDataImpl, AdAutoTestLogImpl autoTestLogImpl,
                       AdStageImpl stageImpl, AdParaDetailImpl paraDetailImpl,
                       AdSeqTestRelateImpl seqTestRelateImpl, AdStageLogDtlImpl adStageLogDtlImpl) {
        this.adBranch = adBranch;
        this.adStage = adStage;
        this.staticDataImpl = staticDataImpl;
        this.autoTestLogImpl = autoTestLogImpl;
        this.adStageImpl = stageImpl;
        this.bsParaDetailImpl = paraDetailImpl;
        this.seqTestRelateImpl = seqTestRelateImpl;
        this.adStageLogDtlImpl = adStageLogDtlImpl;
    }

    @Override
    public void run() {
        System.out.println("--------------------------开始执行自动化测试");
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        boolean isAutoSuc = false;
//        List<AdStaticData> staticDataList = staticDataImpl.qryByCodeType("AUTO_TEST_DETAIL_URL");
        /*String testUrl;
        if (staticDataList != null && staticDataList.size() > 0) {
            testUrl = staticDataList.get(0).getCodeValue();
        } else {
            testUrl = "http://20.26.17.182:9001/searchADCloudTaskResult.do";
        }*/
        List<AdStaticData> dataList = staticDataImpl.qryByCodeType("AUTO_TEST_AD");
        if (dataList != null && dataList.size() > 0) {
            url = dataList.get(0).getCodeValue();
        }
        long startAutoTime = new Date().getTime();
        long seqId = (adStage == null ? 0 : (adStage.getBuildSeq() == null ? 0 : adStage.getBuildSeq()));
        while (!isAutoSuc) {
            // 启动测试任务
            try {
                // WebService
                log.error("****************************************starttest");
                String message = startTest();
                // 处理结果
                JSONArray jsonArray = JSONArray.fromObject("["
                    + message + "]");
                List<Map<Object, Object>> mapListJson = (List<Map<Object, Object>>) jsonArray;
                boolean result = false;
                String getMessage = null;
                String caseCount = "";
                if (mapListJson != null && mapListJson.size() > 0) {
                    result = (Boolean) mapListJson.get(0)
                        .get("Success");
                    getMessage = mapListJson.get(0).get("Message") + "";
                    caseCount = mapListJson.get(0).get("caseCount") + "";
                }
                String[] testIs = null;
                if (StringUtils.isNotEmpty(getMessage)) {
                    testIs = getMessage.split(",");
                }
                if (result) {
                    log.error("autotestend：" + message);
                    isAutoSuc = true;
                    if (testIs != null) {
                        for (String retTest : testIs) {
                            AdSeqTestRelate relate = new AdSeqTestRelate();
                            relate.setState(1);
                            relate.setAdStage(adStage);
                            relate.setCreateDate(new Date());
                            relate.setSeqId(seqId);
                            relate.setTestId(Long.parseLong(retTest));
                            relate.setTestType("ALL");
                            if (StringUtils.isNotEmpty(caseCount)) {
                                relate.setTotalNum(Long.valueOf(caseCount));
                            }
                            seqTestRelateImpl.save(relate);
                            String testUrl = "http://20.26.17.182:9001/searchADCloudTaskResult.do";
                            String testLog = "";
                            Long testId = relate.getTestId();
                            AdAutoTestLog autoTestLog = new AdAutoTestLog();
                            autoTestLog.setBeginDate(relate.getCreateDate());
                            autoTestLog.setEndDate(new Date());
                            autoTestLog.setSeqId(seqId);
                            if (adStage != null) {
                                autoTestLog.setAdBranch(adStage.getAdBranch());
                                autoTestLog.setAdStage(adStage);
                            }
                            autoTestLog.setState(1);
                            autoTestLog.setTotalNum(Long.valueOf(caseCount));
                            autoTestLog.setTestId(relate.getTestId());
                            autoTestLog.setAutoType(1);
                            testLog += "<a href=\"" + testUrl + "?taskId="
                                + testId + "\" target=\"_blank\">查看详情</a>";
                            autoTestLog.setTestLog(testLog);
                            autoTestLogImpl.save(autoTestLog);
                        }
                    }

                } else {
                    if (getMessage.equals("没有空闲主机")) {
                        if (DateConvertUtils.getHourSpace(startAutoTime,
                            new Date().getTime()) > 4) {
                            log.error("autotesterrorend：" + message);
                            try {
                                //置为失败
                                setBack(3);
                                Date date = new Date();
                                AdSeqTestRelate relate = new AdSeqTestRelate();
                                relate.setState(1);
                                relate.setCreateDate(date);
                                relate.setSeqId(seqId);
                                relate.setAdStage(adStage);
//                                relate.setTestId(Long.parseLong(getMessage));
                                relate.setTestType("ALL");
                                if (StringUtils.isNotEmpty(caseCount)) {
                                    relate.setTotalNum(Long.valueOf(caseCount));
                                }
                                seqTestRelateImpl.save(relate);
                                AdAutoTestLog autoTestLog = new AdAutoTestLog();
                                autoTestLog.setCreateDate(date);
                                autoTestLog.setBeginDate(relate.getCreateDate());
                                autoTestLog.setEndDate(date);
                                autoTestLog.setSeqId(seqId);
                                if (adStage != null) {
                                    autoTestLog.setAdBranch(adStage.getAdBranch());
                                }
                                autoTestLog.setAdStage(adStage);
                                autoTestLog.setState(1);
                                autoTestLog.setSucNum(0L);
                                autoTestLog.setFailNum(0L);
                                autoTestLog.setSucPre("0%");
                                autoTestLog.setTotalNum(0L);
                                autoTestLog.setTestId(-1L);
                                autoTestLog.setAutoType(1);
                                autoTestLog.setTestLog("<a href=\"javascript:void(0)\">请求超时</a>");
                                autoTestLogImpl.save(autoTestLog);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        log.error("autotestend2：" + message);
                        //置为失败
                        setBack(3);
                        Date date = new Date();
                        /*AdSeqTestRelate seqTestRelateOld = seqTestRelateImpl.qryBySeqAndCreate(seqId, new Date(), "ALL");
                        if (seqTestRelateOld != null) {
                            seqTestRelateOld.setRemarks(getMessage);
                            seqTestRelateOld.update();
//                                seqTestRelateImpl.save(seqTestRelateOld);
                        } else {*/
                        if (testIs != null) {
                            for (String retTest : testIs) {
                                AdSeqTestRelate relate = new AdSeqTestRelate();
                                relate.setState(1);
                                relate.setCreateDate(new Date());
                                relate.setSeqId(seqId);
                                relate.setAdStage(adStage);
                                relate.setRemarks(retTest);
                                if (StringUtils.isNotEmpty(caseCount)) {
                                    relate.setTotalNum(Long.valueOf(caseCount));
                                }
                                relate.setTestType("ALL");
                                seqTestRelateImpl.save(relate);
                                AdAutoTestLog autoTestLog = new AdAutoTestLog();
                                autoTestLog.setCreateDate(date);
                                autoTestLog.setBeginDate(relate.getCreateDate());
                                autoTestLog.setEndDate(date);
                                autoTestLog.setSeqId(seqId);
                                if (adStage != null) {
                                    autoTestLog.setAdBranch(adStage.getAdBranch());
                                }
                                autoTestLog.setAdStage(adStage);
                                autoTestLog.setState(1);
                                autoTestLog.setSucNum(0L);
                                autoTestLog.setFailNum(0L);
                                autoTestLog.setSucPre("0%");
                                autoTestLog.setTotalNum(0L);
                                autoTestLog.setTestId(-1L);
                                autoTestLog.setAutoType(1);
                                autoTestLog.setTestLog("<a href=\"javascript:void(0)\">" + retTest + "</a>");
                                autoTestLogImpl.save(autoTestLog);

                            }
//                        }
                        }
                    }
                    isAutoSuc = true;
                            /*autoTestLogImpl.saveRelate(seqTask.getSeqId(), getMessage,
                                -1);*/
                }
            } catch (Exception e) {
                e.printStackTrace();
                setBack(3);
                break;
            }
        }
    }


    public String startTest() {
        List<AdStaticData> serviceUrlStaticDataList = staticDataImpl.qryByCodeType("SERVICE_IP");

        String adCloudUrl = AD_CLOUD_IP;
        if (serviceUrlStaticDataList != null && serviceUrlStaticDataList.size() > 0) {
            adCloudUrl = serviceUrlStaticDataList.get(0).getCodeValue();
        }

        String message = "";
        try {
            JaxWsProxyFactoryBean soapFactoryBean = new JaxWsProxyFactoryBean();
            soapFactoryBean.setAddress(url);
            soapFactoryBean.setServiceClass(TestPlatform.class);
            TestPlatform sv = (TestPlatform) soapFactoryBean
                .create();
            Client proxy = ClientProxy.getClient(sv);
            HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
            HTTPClientPolicy policy = new HTTPClientPolicy();
            policy.setConnectionTimeout(300000);
            policy.setReceiveTimeout(500000);
            conduit.setClient(policy);
            String serviceUrl = adCloudUrl + "/autoTest";
            log.error("************************************inputUrl" + url);
            log.error("************************************autotestinputmess" + adStage.getBuildSeq() + "_" + adStage.getStageId() + "_ALL" + "" + adBranch.getBranchName().contains("_qa") + serviceUrl + rmpBranchRelate.getSystemNameTest());
            message = sv.runTask(adStage.getBuildSeq() + "_" + adStage.getStageId() + "_ALL", "", adBranch.getBranchName().contains("_qa") ? 1 : 2
                , serviceUrl, rmpBranchRelate.getSystemNameTest());

            log.error("************************************autotest returnmessage" + message);
        } catch (Exception e) {
            log.error("autotest error", e);
        }
        return message;
    }

    private void setBack(int state) {
        String sql = "UPDATE AD_STAGE SET DEAL_RESULT = :state WHERE STAGE_ID = :stage_id AND STATE != 0 AND DEAL_RESULT != :dealresult";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("state", state);
        update.setParameter("stage_id", adStage.getStageId());
        update.setParameter("dealresult", state);
        Ebean.execute(update);
    }

    public Connection getConnection(String databaseName) throws SQLException {
        AdParaDetail paraDetail = bsParaDetailImpl.qryByDetails("X", databaseName.toUpperCase() + "_DB_INFO", databaseName.toUpperCase() + "_DB_INFO");
//        return DriverManager.getConnection(pro.getValue("datasource." + databaseName + ".databaseUrl"), pro.getValue("datasource." + databaseName + ".username"), pro.getValue("datasource." + databaseName + ".password"));
        return DriverManager.getConnection(paraDetail.getPara1(), paraDetail.getPara2(), paraDetail.getPara3());
    }


}
