package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.common.pojo.pojoExt.FailureBuildInfoExtPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.PersonallyWorkbench;
import com.asiainfo.comm.module.models.AdStaticData;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.comm.module.build.service.impl.AdBuildLogImpl;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.avaje.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by YangRY
 * 2016/7/6 0006.
 */
@RestController
public class QueryBuildController {
    @Autowired
    AdBuildLogImpl buildLogImpl;
    @Autowired
    AdStaticDataImpl staticDataImpl;

    //个人工作台
    @RequestMapping(value = "/personallyMessage", produces = "application/json")
    public String getPersonallyMessage(HttpServletRequest req) {

        //获取当前用户id
        HttpSession httpSession = req.getSession();
        Long userId = 0L;
        if (httpSession.getAttribute("userId") != null) {
            userId = (Long) httpSession.getAttribute("userId");
        }
        if (userId == 0) {
            userId = 1L;
        }

        Map<String, String> builderType = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<AdStaticData> staticDatas = staticDataImpl.qryByCodeType("BUILDER_TYPE");
        for (AdStaticData data : staticDatas) {
            builderType.put(data.getCodeValue(), data.getCodeName());
        }
        //获取错误消息
        List<SqlRow> logs = buildLogImpl.qryPersonalFailedBuildsSqlRow(userId);
        PersonallyWorkbench poj = new PersonallyWorkbench();
        List<FailureBuildInfoExtPojo> pojExts = new ArrayList<>();
        for (SqlRow log : logs) {
            FailureBuildInfoExtPojo pojExt = new FailureBuildInfoExtPojo();
            pojExt.setBranchId(log.getLong("BRANCH_ID"));
            pojExt.setBranchName(log.getString("BRANCH_DESC"));
            pojExt.setProjectId(log.getLong("PROJECT_ID"));
            pojExt.setProjectName(log.getString("PROJECT_NAME"));
            pojExt.setStep(log.getInteger("LAST_STEP"));
            pojExt.setStepName(builderType.get("" + log.getInteger("STAGE_CODE")));
            pojExt.setBuildDate(sdf.format(new Date(log.getDate("BUILD_DATE").getTime())));
            pojExts.add(pojExt);
        }
        poj.setMessage(pojExts);
        return JsonpUtil.modelToJson(poj);
    }

    @RequestMapping(value = "/lastMessage", produces = "application/json")
    public String lastMessage(HttpServletRequest req) {

        //获取当前用户id
        HttpSession httpSession = req.getSession();
        String longName = "";
        if (httpSession.getAttribute("username") != null) {
            longName = (String) httpSession.getAttribute("username");
        }

        Map<String, String> builderType = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<AdStaticData> staticDatas = staticDataImpl.qryByCodeType("BUILDER_TYPE");
        for (AdStaticData data : staticDatas) {
            builderType.put(data.getCodeValue(), data.getCodeName());
        }
        //获取错误消息
        List<SqlRow> logs = buildLogImpl.qryLastFailedBuildsSqlRow(longName);
        PersonallyWorkbench poj = new PersonallyWorkbench();
        List<FailureBuildInfoExtPojo> pojExts = new ArrayList<>();
        for (SqlRow log : logs) {
            FailureBuildInfoExtPojo pojExt = new FailureBuildInfoExtPojo();
            if (CommConstants.BUILD_LOG.BUILD_SUCCESS == log.getInteger("BUILD_RESULT")|| CommConstants.BUILD_LOG.BUILD_BEGINBUILD == log.getInteger("BUILD_RESULT")) {
                continue;
            }
            pojExt.setGroupId(log.getLong("GROUP_ID"));
            pojExt.setGroupName(log.getString("GROUP_NAME"));
            pojExt.setBranchId(log.getLong("BRANCH_ID"));
            pojExt.setBranchName(log.getString("BRANCH_DESC"));
            pojExt.setProjectId(log.getLong("PROJECT_ID"));
            pojExt.setProjectName(log.getString("PROJECT_NAME"));
            pojExt.setStep(log.getInteger("LAST_STEP"));
            pojExt.setStepName(builderType.get("" + log.getInteger("STAGE_CODE")));
            pojExt.setBuildDate(sdf.format(new Date(log.getDate("BUILD_DATE").getTime())));
            pojExts.add(pojExt);
        }
        poj.setMessage(pojExts);
        return JsonpUtil.modelToJson(poj);
    }
}
