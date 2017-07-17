package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.common.pojo.pojoMaster.OperationNowPojo;
import com.asiainfo.comm.module.build.dao.impl.*;
import com.asiainfo.comm.module.build.service.impl.AdOperationImpl;
import com.asiainfo.comm.module.build.service.impl.AdSonarImpl;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.schedule.helper.UploadArtifactoryRunnable;
import com.asiainfo.util.JsonpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by YangRY
 * 2016/6/12 0012.
 */
@RestController
@RequestMapping(value = "/test")
public class TestDevOpsController {
    @Autowired
    AdSonarImpl adSonarImpl;
    @Autowired
    AdStageDAO adStageDAO;
    @Autowired
    AdProjectDAO adProjectDAO;
    @Autowired
    AdStaticDataImpl bsStaticImpl;
    @Autowired
    private AdBranchDAO branchDAO;
    @Autowired
    private AdJenkinsInfoDAO jenkinsInfoDAO;
    @Autowired
    private AdProjectDeployPackageDAO packageDAO;
    @Value("${artifactory.user.url}")
    private String artifactoryUrl;

    @Value("${artifactory.user.name}")
    private String artifactoryName;

    @Value("${artifactory.user.password}")
    private String artifactoryPassword;

    @RequestMapping(value = "/testDoUser")
    public String testDoUser() {
        System.out.println("---");
        OperationNowPojo pojo = null;
        try {
            pojo = new AdOperationImpl().qryPips(1);
        } catch (Exception e) {
            e.printStackTrace();
            pojo = new OperationNowPojo();
            pojo.setRetCode("500");
            pojo.setRetMessage(e.getMessage());
        } finally {
            if (pojo == null) {
                pojo = new OperationNowPojo();
            }
            return JsonpUtil.modelToJsonp(pojo);
        }
    }


    @RequestMapping(value = "/testUploadToARTIFactory")
    public String testUploadToARTIFactory() {
        UploadArtifactoryRunnable run = new UploadArtifactoryRunnable(6, 22, "324cd2d9651e24fcffccaf956f5c585753e38f1e",
            branchDAO, jenkinsInfoDAO,
            packageDAO, adStageDAO, artifactoryUrl, artifactoryName, artifactoryPassword, bsStaticImpl);
        run.run();
        return "SUCCESS";
    }

    @RequestMapping(value = "/getTime")
    public String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        return sdf.format(new Date());
    }
}
