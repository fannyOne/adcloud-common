package com.asiainfo.comm.module.build.controller;

import com.asiainfo.auth.sso.gitlib.api.models.GitlabProject;
import com.asiainfo.comm.common.pojo.pojoExt.AdDcosDeployDtlExt;
import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.util.JsonUtil;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.comm.module.build.service.impl.GitServiceImpl;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangry on 2016/6/16 0016.
 */

//@SpringApplicationConfiguration(classes = GitController.class)
@lombok.extern.slf4j.Slf4j
@RestController
public class GitController {
    /*@Autowired
    HttpServletRequest request;*/
    @Autowired
    GitServiceImpl gitService;
    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;


    @RequestMapping(value = "/gitHookBuild", method = RequestMethod.POST)
    public String gitHookBuild(@RequestBody Map param) throws Exception {
        System.out.println(param.toString());
        JSONObject json = new JSONObject().fromObject(param);
        gitService.gitHookBuild(json, param);
        return "hello";
    }

    private String inputToStream(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input);
        BufferedReader br = new BufferedReader(reader);
        StringBuffer sb = new StringBuffer();
        String s = "";
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }
        String str = sb.toString();
        return str;
    }

    @RequestMapping(value = "/GitCheck", produces = "application/json")
    public String GitCheck(@RequestParam Map map, HttpServletRequest req) {
        Map retMap = new HashMap<>();
        retMap.put("isexits", gitService.gitCheckPre(map, req));
        return JsonUtil.mapToJson(retMap);
    }

    @RequestMapping(value = "/qryProjectTag", produces = "application/json")
    public String qryProjectTag(@RequestParam Map map) {
        String retvalue = "";
        long projectId;
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            if (map != null && map.get("projectid") != null) {
                projectId = Long.valueOf((String) map.get("projectid"));
            } else {
                throw new Exception("项目编号不正确");
            }

        /* 资源隔离，权限验证 */
            if (!userRoleRelImpl.verifyPurview("projectId", projectId)) {
                return null;
            }
        /* 资源隔离，权限验证 Over */

            retMap = gitService.qryProjectTag(projectId);
        } catch (Exception e) {
            retMap.put("code", "500");
            retMap.put("message", e.getMessage());
        }
        retvalue = JsonUtil.mapToJson(retMap);
        return retvalue;
    }

    @RequestMapping(value = "/getInitTagName", method = RequestMethod.POST)
    public String getInitTagName(@RequestParam Map map) throws Exception {
        String retvalue = "";
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            long branchId;
            if (map != null && map.get("branchId") != null) {
                branchId = Long.valueOf((String) map.get("branchId"));
            } else {
                throw new Exception("分支编号不正确");
            }
            String tagName = gitService.getInitTagName(branchId);
            retMap.put("tagName", tagName);
            retMap.put("code", "200");
        } catch (Exception e) {
            throw e;
        }
        retvalue = JsonUtil.mapToJson(retMap);
        return retvalue;
    }


    @RequestMapping(value = "/addProjectTag", produces = "application/json")
    public String addProjectTag(@RequestParam Map map) throws Exception {
        String retvalue = "";
        long branchId;
        String tagName;
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            if (map != null && map.get("branchId") != null) {
                branchId = Long.valueOf((String) map.get("branchId"));
            } else {
                throw new Exception("分支编号不正确");
            }
                   /* 资源隔离，权限验证 */
            if (!userRoleRelImpl.verifyPurview("branchId", branchId)) {
                return null;
            }
        /* 资源隔离，权限验证 Over */
            if (map.get("tagName") != null) {
                tagName = (String) map.get("tagName");
            } else {
                throw new Exception("分支名称不正确");
            }
            gitService.addProjectTag(branchId, tagName);
            retMap.put("code", "200");
        } catch (Exception e) {
            throw e;
        }
        retvalue = JsonUtil.mapToJson(retMap);
        return retvalue;
    }


    @RequestMapping(value = "/qryProjectVersion", produces = "application/json")
    public String qryProjectVersion(@RequestParam Map map) throws Exception {
        String retvalue = "";
        long projectId;
        int buildType;
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (map == null) {
            return null;
        }
        try {
            if (map.get("projectId") != null) {
                projectId = Long.valueOf((String) map.get("projectId"));
            } else {
                throw new Exception("项目编号不正确");
            }
        /* 资源隔离，权限验证 */
            if (!userRoleRelImpl.verifyPurview("projectId", projectId)) {
                return null;
            }
        /* 资源隔离，权限验证 Over */
            if (map.get("buildType") != null) {
                buildType = Integer.valueOf((String) map.get("buildType"));
            } else {
                throw new Exception("构建类型不正确");
            }
            if (map.containsKey("envId")) {
                String envIdStr = (String) map.get("envId");
                Long envId;
                String envType;
                String[] envIdStrList = envIdStr.split("_");
                envId = Long.parseLong(envIdStrList[0]);
                envType = envIdStrList[1];
                retMap.put("version", gitService.qryProjectVersion(projectId, buildType, envId, envType));
                List<AdDcosDeployDtlExt> adDcosDeployDtlExtList = gitService.qryDcosDeployDtl(envId, envType);
                if (adDcosDeployDtlExtList != null) {
                    retMap.put("appId", adDcosDeployDtlExtList);
                }
            } else {
                retMap.put("version", gitService.qryProjectVersion(projectId, buildType));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        retvalue = JsonUtil.mapToJson(retMap);
        return retvalue;
    }

    @RequestMapping(value = "/qryProjectVersionDesc", produces = "application/json")
    public String qryProjectVersionDesc(@RequestParam Map map) throws Exception {
        String retvalue = "";
        long projectId;
        int buildType;
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (map == null) {
            return null;
        }
        try {
            if (map.get("projectId") != null) {
                projectId = Long.valueOf((String) map.get("projectId"));
            } else {
                throw new Exception("项目编号不正确");
            }
        /* 资源隔离，权限验证 */
            if (!userRoleRelImpl.verifyPurview("projectId", projectId)) {
                return null;
            }
        /* 资源隔离，权限验证 Over */
            if (map.get("buildType") != null) {
                buildType = Integer.valueOf((String) map.get("buildType"));
            } else {
                throw new Exception("构建类型不正确");
            }
            if (map.containsKey("envId")) {
                String envIdStr = (String) map.get("envId");
                Long envId;
                String envType;
                String[] envIdStrList = envIdStr.split("_");
                envId = Long.parseLong(envIdStrList[0]);
                envType = envIdStrList[1];
                retMap.put("version", gitService.qryProjectVersionDesc(envId, envType));
                List<AdDcosDeployDtlExt> adDcosDeployDtlExtList = gitService.qryDcosDeployDtl(envId, envType);
                if (adDcosDeployDtlExtList != null) {
                    retMap.put("appId", adDcosDeployDtlExtList);
                }
            } else {
                retMap.put("version", gitService.qryProjectVersion(projectId, buildType));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        retvalue = JsonUtil.mapToJson(retMap);
        return retvalue;
    }

    @RequestMapping(value = "/qryGitAllProjects", produces = "application/json")
    public List<Map<String, String>> qryGitAllProjects(@RequestParam Map map, HttpServletRequest req) {
        String privateToken = (String) req.getSession().getAttribute("privateToken");
        List<Map<String, String>> gitlabProjects = gitService.qryGitAllProjects(privateToken);
        return gitlabProjects;
    }

    @RequestMapping(value = "/newGitProject", produces = "application/json")
    public String newGitProject(@RequestParam Map map, HttpServletRequest req) throws IOException {
        CommonPojo poj = new CommonPojo();
        String projectName = (String) map.get("projectName");
        String privateToken = (String) req.getSession().getAttribute("privateToken");
        GitlabProject gitlabProject = gitService.createUserProject(req,privateToken, projectName);
        poj.setRetMessage(gitlabProject.getHttpUrl());
        return JsonpUtil.modelToJson(poj);
    }
}
