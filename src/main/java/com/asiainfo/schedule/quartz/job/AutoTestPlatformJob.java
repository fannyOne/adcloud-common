package com.asiainfo.schedule.quartz.job;

import com.asiainfo.comm.module.autoTest.service.impl.AdAutoTestLogImpl;
import com.asiainfo.comm.module.build.service.impl.AdBuildTaskRelateImpl;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.comm.module.models.AdBuildTaskRelate;
import com.asiainfo.comm.module.models.AdStaticData;
import com.asiainfo.comm.module.webService.TestPlatform;
import com.asiainfo.util.DateConvertUtils;
import net.sf.json.JSONArray;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.asiainfo.util.CommConstants.BuildConstants.AD_CLOUD_IP;
import static com.asiainfo.util.CommConstants.BuildConstants.AD_CLOUD_PORT;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 * 触发自动化测试的程序
 */
public class AutoTestPlatformJob implements Job {
    @Autowired
    AdStaticDataImpl staticDataImpl;
    @Autowired
    AdAutoTestLogImpl autoTestLogImpl;
    @Autowired
    AdBuildTaskRelateImpl buildTaskRelateImpl;



    private String taskCode;

    private int codeNum;

    private long buildSeq;

    private String url;

    private long startAutoTime = 0;

    private boolean isAutoSuc = false;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        startDealInfo(jobExecutionContext);
    }

    public String startDealInfo(JobExecutionContext jobExecutionContext) {
        // TODO Auto-generated method stub
        JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
        taskCode = map.getString("taskCode");
        codeNum = map.getInt("codeNum");
        buildSeq = map.getLong("buildSeq");
        List<AdStaticData> datas = staticDataImpl.qryByCodeType("AUTO_TEST_AD");
        if (datas != null && datas.size() > 0) {
            url = datas.get(0).getCodeValue();
        }
        Set<String> taskCodeMap = new HashSet<String>();
        long startAutoTime = 0;
        boolean isAutoSuc = false;
        String returnStr = "success";
        // 根据seqId获得构建流水和需求单关系数据
        List<AdBuildTaskRelate> seqTasks = buildTaskRelateImpl
            .getSeqTaskByPubSeqId(buildSeq);
        if (seqTasks != null && seqTasks.size() > 0) {
            for (AdBuildTaskRelate seqTask : seqTasks) {
                String taskCode = null;
                if (taskCode != null) {
                    if (taskCodeMap.contains(taskCode)) {
                        continue;
                    }
                    taskCodeMap.add(taskCode);
                    isAutoSuc = false;
                    startAutoTime = new Date().getTime();
                    while (!isAutoSuc) {
                        // 启动测试任务
                        String message = startTest();
                        System.out.println("测试任务返回结果：" + message);
                        // 处理结果
                        JSONArray jsonArray = JSONArray.fromObject("["
                            + message + "]");
                        List<Map<Object, Object>> mapListJson = (List<Map<Object, Object>>) jsonArray;
                        boolean result = false;
                        String getMessage = null;
                        if (mapListJson != null && mapListJson.size() > 0) {
                            result = (Boolean) mapListJson.get(0)
                                .get("Success");
                            getMessage = mapListJson.get(0).get("Message") + "";
                        }
                        if (result) {
                            isAutoSuc = true;
                        } else {
                            if (getMessage.equals("没有空闲主机")) {
                                if (DateConvertUtils.getHourSpace(startAutoTime,
                                    new Date().getTime()) > 4) {
                                    returnStr = "failed";
                                    break;
                                } else {
                                    continue;
                                }
                            }
                            isAutoSuc = true;
                            returnStr = "failed";
                        }
                    }
                }
            }
        }
        return returnStr;
    }

    public String startTest() {
        JaxWsProxyFactoryBean soapFactoryBean = new JaxWsProxyFactoryBean();
        soapFactoryBean.setAddress(url);
        soapFactoryBean.setServiceClass(TestPlatform.class);
        TestPlatform sv = (TestPlatform) soapFactoryBean
            .create();
        String serviceUrl = "http://" + AD_CLOUD_IP + ":" + AD_CLOUD_PORT;
        String message = sv.runTask(buildSeq + "", taskCode, codeNum, serviceUrl, "");
        return message;
    }
}

