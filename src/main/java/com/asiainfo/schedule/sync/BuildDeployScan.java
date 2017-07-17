package com.asiainfo.schedule.sync;

import com.asiainfo.comm.module.build.dao.impl.AdProjectDAO;
import com.asiainfo.comm.module.build.service.impl.AdBuildDeployDataImpl;
import com.asiainfo.comm.module.build.service.impl.AdBuildDeployReportImpl;
import com.asiainfo.comm.module.build.service.impl.AdSonarImpl;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.comm.module.models.AdProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.List;

/**
 * Created by zhangpeng on 2016/7/12.
 */
@lombok.extern.slf4j.Slf4j
@Component
public class BuildDeployScan {
    @Autowired
    AdBuildDeployDataImpl adBuildDeployData;
    @Autowired
    AdBuildDeployReportImpl adBuildDeployReport;
    @Autowired
    AdSonarImpl adSonar;
    @Autowired
    AdStaticDataImpl bsStaticDataImpl;
    @Autowired
    AdProjectDAO adProjectDAO;

    @Async
    public void getBuildDeploydata() throws ParseException {//TODO 获取构建和部署信息
                /*
         * 进程唯一性处理
         */
/*        List<AdStaticData> data = bsStaticDataImpl.qryByCodeType("SYNC_RMP");
        if (data == null || data.size() <= 0 || data.get(0).getCodeValue() == null || !data.get(0).getCodeValue().equals(AD_CLOUD_IP)) {
            return;
        }*/
        /*
         * 进程唯一性处理结束
         */
//       System.out.println("11111111111111111111111111");
//       System.out.println( adBuildDeployReport.getMonthlyFirstDay("yyyy-MM"));
        List<AdProject> adProjectList = adProjectDAO.getAllSystem();
        for (AdProject adProject : adProjectList) {
            adBuildDeployData.getBuildLogDeployData(adProject.getProjectId());
//            int curData = adSonar.getCurData();
//            System.out.println("curData=====" + curData);
//            if (curData == 5) {//TODO 计算周五周报信息
//                System.out.println("计算周五周报信息！！！！！！");
//              adBuildDeployData.getBuildByName(jobName[i]);
//            } else if (curData == 3) {
//                adBuildDeployReport.getBuildReportMonthly(jobName[i]);
//            } else if (curData == 2) {//TODO 当前是最后一天，并且是周五。计算月报信息
//                System.out.println("计算周五并计算月报信息！！！！！！");
//                adBuildDeployData.getBuildByName(jobName[i]);
//               adBuildDeployReport.getBuildReportMonthly(jobName[i]);
//            }
        }
    }
}
