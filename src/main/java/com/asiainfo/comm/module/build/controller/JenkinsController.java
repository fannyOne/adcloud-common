package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.common.pojo.pojoMaster.ManualHandPojo;
import com.asiainfo.util.JsonUtil;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.comm.module.build.service.impl.AdProjectGitTagsImpl;
import com.asiainfo.comm.module.build.service.impl.GitServiceImpl;
import com.asiainfo.comm.module.build.service.impl.JenkinsImpl;
import com.asiainfo.comm.module.models.AdProjectGitTags;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.asiainfo.comm.module.role.service.impl.VerifyRightImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YangRY
 * on 2016/6/23 0023.
 */
@RestController
public class JenkinsController {
    @Autowired
    JenkinsImpl jenkinsService;
    @Autowired
    GitServiceImpl gitService;
    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;
    @Autowired
    AdProjectGitTagsImpl adProjectGitTagsImpl;
    @Autowired
    VerifyRightImpl verifyRightImpl;

    /*
       根据环境编号和构建类型触发Jenkins构建
       */
    @RequestMapping(value = "/ManualHand", produces = "application/json")
    public String ManualHand(@RequestParam Map map, HttpServletRequest request) throws IOException {
        long envId = 0;
        int buildType = 0;
        if (map.get("envId") != null) {
            envId = Long.valueOf((String) map.get("envId"));
        }
        //操作权限限制
        if (!verifyRightImpl.verifyBranchRight(envId, (String) request.getSession().getAttribute("username"))) {
            return null;
        }
        /* 资源隔离，权限验证 Over */
        if (map.get("buildType") != null) {
            buildType = Integer.valueOf((String) map.get("buildType"));
        }
        String retvalue;
        HttpSession httpSession = request.getSession();
        Long userId = 0L;
        if (httpSession.getAttribute("userId") != null) {
            userId = (Long) httpSession.getAttribute("userId");
        }
        if (userId == 0) {
            userId = 1L;
        }
        ManualHandPojo poj = jenkinsService.triggerJenkins(envId, buildType, userId + "", false, "");
        retvalue = JsonpUtil.modelToJson(poj);
        return retvalue;
    }

    @RequestMapping(value = "/StopPipeline", produces = "application/json")
    public String StopPipeline(@RequestParam Map map, HttpServletRequest request) throws IOException {
        long envId = 0;
        int buildType = 0;
        String retvalue = "";
        try {
            Map retMap = new HashMap<>();
            if (map.get("envId") != null) {
                envId = Long.valueOf((String) map.get("envId"));
            }
        /* 资源隔离，权限验证 */
            if (!verifyRightImpl.verifyBranchRight(envId, (String) request.getSession().getAttribute("username"))) {
                return null;
            }
        /* 资源隔离，权限验证 Over */
            if (map.get("buildType") != null) {
                buildType = Integer.valueOf((String) map.get("buildType"));
            }
            HttpSession httpSession = request.getSession();
            Long userId = 0L;
            if (httpSession.getAttribute("userId") != null) {
                userId = (Long) httpSession.getAttribute("userId");
            }
            if (userId == 0) {
                userId = 1L;
            }
            jenkinsService.StopPipeline(envId, buildType);
            retMap.put("code", "200");
            retvalue = JsonUtil.mapToJson(retMap);
        } catch (Exception e) {
            throw e;
        }
        return retvalue;
    }

    @RequestMapping(value = "/HandProdPipline", produces = "application/json")
    public String HandProdPipline(@RequestParam Map map, HttpServletRequest request) throws IOException {
        long envId = 0;
        int buildType = 0;
        String commitId = "";
        if (map.get("envId") != null) {
            envId = Long.valueOf((String) map.get("envId"));
        }
        /* 资源隔离，权限验证 */
        if (!userRoleRelImpl.verifyPurview("branchId", envId)) {
            return null;
        }
        /* 资源隔离，权限验证 Over */
        String retvalue = "";
        try {
            if (map.get("buildType") != null) {
                buildType = Integer.valueOf((String) map.get("buildType"));
            }
            if (map.get("proTagId") != null) {
                AdProjectGitTags adProjectGitTags = adProjectGitTagsImpl.qryByProTagId(Long.valueOf((String) map.get("proTagId")));
                if (adProjectGitTags != null) {
                    commitId = adProjectGitTags.getCommitId();
                }
            }
            System.out.print("进入" + envId + "&" + buildType);

            HttpSession httpSession = request.getSession();
            Long userId = 0L;
            if (httpSession.getAttribute("userId") != null) {
                userId = (Long) httpSession.getAttribute("userId");
            }
            if (userId == 0) {
                userId = 1L;
            }
            ManualHandPojo commonPojo = jenkinsService.triggerJenkins(envId, buildType, userId + "", true, commitId);
            retvalue = JsonpUtil.modelToJson(commonPojo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return retvalue;
    }

    // 查询Jenkins配置
    @RequestMapping(value = "/JenkinsConfig", produces = "application/json")
    public String JenkinsConfig(@RequestParam Map map, HttpServletRequest request) {
        long begin_time = System.currentTimeMillis();
        Map retMap = new HashMap<>();
        try {
            if (map.get("branchId") != null) {
                long branchId = Long.valueOf((String) map.get("branchId"));
                retMap = jenkinsService.qryJenkinsConfig(branchId);
            } else {
                retMap.put("code", "500");
                retMap.put("message", "入参不正确" + map);
            }

        } catch (Exception e) {
            retMap.put("code", "500");
            retMap.put("message", e.getMessage());
            e.printStackTrace();
        }
        long end_time = System.currentTimeMillis();
        System.out.print("***************" + (end_time - begin_time));
        String retvalue = JsonUtil.mapToJson(retMap);
        return retvalue;
    }

    @RequestMapping(value = "/ManualDeploy", produces = "application/json")
    public String ManualDeploy(@RequestParam Map map) {
        String retvalue = "";
        long projectId = 0;
        String tag = "";
        Map retMap = new HashMap<>();
        retMap.put("code", "500");
        try {
            if (map.get("projectid") != null && map.get("projectid") != null && StringUtils.isNotEmpty((String) map.get("projectid"))) {
                projectId = Long.valueOf((String) map.get("projectid"));

        /* 资源隔离，权限验证 */
                if (!userRoleRelImpl.verifyPurview("projectId", projectId)) {
                    return null;
                }
        /* 资源隔离，权限验证 Over */

            } else {
                throw new Exception("入参项目编号不正确");
            }
            if (map.get("tag") != null && map.get("tag") != null) {
                tag = (String) map.get("tag");
            } else {
                throw new Exception("入参标签不正确");
            }
            boolean successFlag = jenkinsService.ManualDeploy(projectId, tag);
            if (successFlag) {
                retMap.put("code", "200");
            } else {
                retMap.put("message", "调用jenkins失败");
            }
        } catch (Exception e) {
            retMap.put("message", e.getMessage());
        }
        retvalue = JsonUtil.mapToJson(retMap);
        return retvalue;
    }

}
