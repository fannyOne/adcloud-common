package com.asiainfo.comm.module.deploy.controller;


import com.asiainfo.comm.common.pojo.DefaultPojo;
import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.*;
import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.JsonUtil;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.util.MapUtil;
import com.asiainfo.comm.module.build.controller.GitController;
import com.asiainfo.comm.module.build.dao.impl.AdPipeLineStateDAO;
import com.asiainfo.comm.module.build.dao.impl.AdStageDAO;
import com.asiainfo.comm.module.build.service.impl.*;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.role.controller.GroupUserConverter;
import com.asiainfo.comm.module.role.service.impl.AdAuthorImpl;
import com.asiainfo.comm.module.role.service.impl.AdGroupAdminUserImpl;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.avaje.ebean.SqlRow;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weif on 2016/7/25.
 */
@RestController
@lombok.extern.slf4j.Slf4j
public class SystemDeployController {
    @Autowired
    SystemDeployImpl systemDeploy;
    @Autowired
    JenkinsImpl jenkins;
    @Autowired
    GitServiceImpl gitServiceImpl;
    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;
    @Autowired
    AdAuthorImpl adAuthorImpl;
    @Autowired
    GroupUserConverter groupUserConverter;
    @Autowired
    AdGroupImpl adgroupImpl;

    @Autowired
    AdGroupAdminUserImpl adGroupAdminUserImpl;
    @Autowired
    AdOperationImpl adOperationImpl;
    @Autowired
    AdPipeLineStateDAO stateDAO;
    @Autowired
    AdBranchImpl adbranchImpl;
    @Autowired
    AdStageDAO adStageDAO;

    @Autowired
    GitController gitController;


    @RequestMapping(value = "/AddGroup", produces = "application/json")
    public String AddGroup(@RequestParam Map map, HttpServletRequest request) {
        String groupName = "";
        String groupDesc = "";
        String retvalue = "";
        long groupId = 0;
        AdGroup addGroup = new AdGroup();
        Map<String, Object> hmap = new HashMap<String, Object>();
        try {
            if (map != null) {
                if (map.get("groupName") != null && StringUtils.isNotEmpty((String) map.get("groupName"))) {
                    groupName = (String) map.get("groupName");
                } else {
                    throw new Exception("参数输入不正确");
                }
                if (map.get("groupDesc") != null && StringUtils.isNotEmpty((String) map.get("groupDesc"))) {
                    groupDesc = (String) map.get("groupDesc");
                } else {
                    throw new Exception("参数输入不正确");
                }
                if (map.get("groupId") != null && StringUtils.isNotEmpty((String) map.get("groupId"))) {
                    groupId = Long.parseLong((String) map.get("groupId"));
                }
                if (groupId != 0) {
                    addGroup.setGroupId(groupId);
                    addGroup.setGroupName(groupName);
                    addGroup.setGroupDesc(groupDesc);
                    systemDeploy.updateGroup(addGroup);
                } else {
                    addGroup.setGroupName(groupName);
                    addGroup.setGroupDesc(groupDesc);
                    String userName = (String) request.getSession().getAttribute("username");
                    GroupUserMemberPojo groupUserMemberPojo = new GroupUserMemberPojo();
                    groupUserMemberPojo.setUserName(userName);
                    groupUserMemberPojo.setPm(true);
                    addGroup = systemDeploy.addGroup(addGroup, groupUserConverter.converterToAdGroupUser(groupUserMemberPojo));
                    Long userId = (Long) request.getSession().getAttribute("userId");
                    if (userRoleRelImpl.isGroupAdmin(userName)) {
                        adGroupAdminUserImpl.create(userId, addGroup.getGroupId(), userId, userName);
                    }
                }
            } else {
                throw new Exception("参数输入不正确");
            }
            if (addGroup == null) {
                throw new Exception("新增未成功");
            }
            hmap.put("code", "200");
            hmap.put("groupId", "" + addGroup.getGroupId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            hmap.put("code", "500");
            hmap.put("message", e.getMessage());
        }
        retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }

    @RequestMapping(value = "/AddProject", produces = "application/json")
    public String AddProject(@RequestParam Map map, HttpServletRequest request) {
        String projectName = "";
        String codeStore = "";
        int groupId = 0;
        String retvalue = "";
        String compileTool;
        String compileToolVersion;
        String buildTool;
        Map<String, Object> hmap = new HashMap<String, Object>();
        try {
            if (map != null) {
                if (map.get("projectName") != null && StringUtils.isNotEmpty((String) map.get("projectName"))) {
                    projectName = (String) map.get("projectName");
                } else {
                    throw new Exception("参数输入不正确");
                }
                if (map.get("codeStore") != null && StringUtils.isNotEmpty((String) map.get("codeStore"))) {
                    codeStore = (String) map.get("codeStore");
                } else {
                    throw new Exception("参数输入不正确");
                }
                if (map.get("groupId") != null && StringUtils.isNotEmpty((String) map.get("groupId"))) {
                    groupId = Integer.parseInt((String) map.get("groupId"));
                } else {
                    throw new Exception("参数输入不正确");
                }
                if (map.get("compileTool") != null) {
                    compileTool = (String) map.get("compileTool");
                    if (compileTool.split("_").length > 1) {
                        compileTool = compileTool.split("_")[0];
                    }
                } else {
                    throw new Exception("参数输入不正确");
                }
                if (map.get("compileToolVersion") != null) {
                    compileToolVersion = (String) map.get("compileToolVersion");
                } else {
                    throw new Exception("参数输入不正确");
                }
                if (map.get("buildTool") != null) {
                    buildTool = (String) map.get("buildTool");
                    if (buildTool.split("_").length > 1) {
                        buildTool = buildTool.split("_")[0] + " " + buildTool.split("_")[1];
                    }
                } else {
                    throw new Exception("参数输入不正确");
                }
            } else {
                throw new Exception("参数输入不正确");
            }
            //校验gitprojectid是否存在
            String privateToken = (String) request.getSession().getAttribute("privateToken");
            int getGitProjectId = systemDeploy.getGitProjectId(codeStore, privateToken);
            if (getGitProjectId <= 0) {
                throw new Exception("根据输入的：" + codeStore + "未能在gitlab上找到对应的项目，请检查ULR和您的gitlab账号权限");
            }
            HttpSession httpSession = request.getSession();
            String username = null;
            long opId = 0;
            if (httpSession.getAttribute("username") != null) {
                username = (String) httpSession.getAttribute("username");
                opId = (Long) httpSession.getAttribute("userId");
            }
            if (username != null) {
                long roleId = 0;
                List<AdUserRoleRel> adAuthorList = userRoleRelImpl.qryByUser(username);
                if (adAuthorList != null && adAuthorList.size() > 0) {
                    roleId = (adAuthorList.get(0)).getAdRole().getRoleId();
                }

                ProjectInputExtPojo projectInputExtPojo = new ProjectInputExtPojo();
                projectInputExtPojo.setCodeStore(codeStore);
                projectInputExtPojo.setGroupId(groupId);
                projectInputExtPojo.setOpId(opId);
                projectInputExtPojo.setProjectName(projectName);
                projectInputExtPojo.setRoleId(roleId);
                projectInputExtPojo.setBuildTool(buildTool);
                projectInputExtPojo.setCompileTool(compileTool);
                projectInputExtPojo.setCompileToolVersion(compileToolVersion);
                projectInputExtPojo.setGitProjectid(getGitProjectId);
                systemDeploy.addProject(projectInputExtPojo);
                List<Map<String, String>> adProjectList = systemDeploy.qryProjectList(groupId);
                hmap.put("projects", adProjectList);
                hmap.put("code", "200");
            } else {
                throw new Exception("用户权限信息异常");
            }
        } catch (Exception e) {
            hmap.put("code", "500");
            hmap.put("message", e.getMessage());
        }
        retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }


    @RequestMapping(value = "/QryCurProject", produces = "application/json")
    public String QryCurProject(@RequestParam Map map) {
        String retvalue = "";
        Map<String, Object> hmap = new HashMap<String, Object>();
        Long groupId = 0L;
        try {
            if (map.get("groupId") != null && StringUtils.isNotEmpty((String) map.get("groupId"))) {
                groupId = Long.parseLong((String) map.get("groupId"));
            } else {
                throw new Exception("参数输入不正确");
            }
            if (null == adgroupImpl.qryById(groupId)) {
                hmap.put("isExsit", Boolean.FALSE);
                throw new Exception("根据groupId:" + groupId + "未查到对应的项目信息");
            }
            List<Map<String, String>> adProjectList = systemDeploy.qryProjectList(groupId.intValue());
            hmap.put("projects", adProjectList);
            hmap.put("projects", adProjectList);
            hmap.put("code", "200");
        } catch (Exception e) {
            hmap.put("code", "500");
            hmap.put("message", e.getMessage());
        }
        retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }


    @RequestMapping(value = "/qryDefaultStage", produces = "application/json")
    public String qryDefaultStage(@RequestParam Map map) {
        int branchType = 0;
        String retvalue = "";
        Map<String, Object> hmap = new HashMap<String, Object>();
        try {
            if (map != null) {
                if (map.get("branchType") != null && StringUtils.isNotEmpty((String) map.get("branchType"))) {
                    branchType = Integer.parseInt((String) map.get("branchType"));
                } else {
                    throw new Exception("参数输入不正确");
                }
                List<AdStaticData> adStaticDataList = systemDeploy.qryDefaultStage();
                hmap.put("defaultstage", adStaticDataList);
                hmap.put("code", "200");
            }
        } catch (Exception e) {
            hmap.put("code", "500");
            hmap.put("message", e.getMessage());
        }
        retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }


    @RequestMapping(value = "/qryBranchByProjectId", produces = "application/json")
    public AdBuildTriggerPojoExt qryBranchByProjectId(@RequestParam Map map) {
        AdBuildTriggerPojoExt adBuildTriggerPojoExt = new AdBuildTriggerPojoExt();
        List<Map<String, String>> branchList = new ArrayList<>();
        Map<String, String> branchMap = null;
        long projectId = Long.parseLong((String) map.get("projectId"));
        long branchType = Long.parseLong((String) map.get("branchType"));
        List<SqlRow> adBranchList = adOperationImpl.qryBranchByProjectId(projectId, branchType);
        for (SqlRow sqlRowList : adBranchList) {
            branchMap = new HashMap<>();
            branchMap.put("branchId", sqlRowList.getString("branch_id"));
            branchMap.put("branchName", sqlRowList.getString("branch_desc"));
            branchList.add(branchMap);
        }
        adBuildTriggerPojoExt.setBranchList(branchList);
        return adBuildTriggerPojoExt;
    }

    @RequestMapping(value = "/qryBranchByBranchType", produces = "application/json")
    public AdBuildTriggerPojoExt qryBranchByBranchType(@RequestParam Map map) {
        AdBuildTriggerPojoExt adBuildTriggerPojoExt = new AdBuildTriggerPojoExt();
        List<Map<String, String>> branchList = new ArrayList<>();
        Map<String, String> branchMap = null;
        long projectId = MapUtil.getLongValue(map, "projectId");
        String branchType = MapUtil.getValue(map, "branchType");
        List<SqlRow> adBranchList = adOperationImpl.qryBranchByBranchType(projectId, branchType);
        for (SqlRow sqlRowList : adBranchList) {
            branchMap = new HashMap<>();
            branchMap.put("branchId", sqlRowList.getString("branch_id"));
            branchMap.put("branchType", sqlRowList.getString("branch_type"));
            branchMap.put("branchName", sqlRowList.getString("branch_desc"));
            branchList.add(branchMap);
        }
        adBuildTriggerPojoExt.setBranchList(branchList);
        return adBuildTriggerPojoExt;
    }

    @RequestMapping(value = "/qryStageJkInfo", produces = "application/json")
    public String qryStageJkInfo(@RequestParam Map map) {
        String retvalue = "";
        long branchId;
        long projectId;
        try {
            if (map != null) {
                if (map.get("branchId") != null && StringUtils.isNotEmpty(map.get("branchId").toString())) {
                    branchId = Long.parseLong(map.get("branchId").toString());
                } else {
                    throw new Exception("branchId参数输入不能为空");
                }

        /* 资源隔离，权限验证 */
                if (!userRoleRelImpl.verifyPurview("branchId", branchId)) {
                    return null;
                }
        /* 资源隔离，权限验证 Over */

                SysReformBranchPojoExt sysBranchPojoExt = systemDeploy.qryStageJkInfo(branchId);
                retvalue = JsonpUtil.modelToJson(sysBranchPojoExt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retvalue;
    }

    @RequestMapping(value = "/qrySystemDeploy", produces = "application/json")
    public String qrySystemDeploy(@RequestParam Map map) {
        String retvalue = "";
        long projectId;
        try {
            if (map != null) {
                if (map.get("projectId") != null && StringUtils.isNotEmpty((String) map.get("projectId"))) {
                    projectId = Long.parseLong((String) map.get("projectId"));
                } else {
                    throw new Exception("参数输入不正确");
                }

        /* 资源隔离，权限验证 */
                if (!userRoleRelImpl.verifyPurview("projectId", projectId)) {
                    return null;
                }
        /* 资源隔离，权限验证 Over */

                SysReformProjectPojoExt sysProjectPojoExt = systemDeploy.qrySystemDeploy(projectId);
                retvalue = JsonpUtil.modelToJson(sysProjectPojoExt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retvalue;
    }


    @RequestMapping(value = "/delGroup", produces = "application/json")
    public String delGroup(@RequestParam Map map) {
        int groupId = 0;
        String retvalue = "";
        Map<String, Object> hmap = new HashMap<String, Object>();
        try {
            if (map != null) {
                if (map.get("groupId") != null && StringUtils.isNotEmpty((String) map.get("groupId"))) {
                    groupId = Integer.parseInt((String) map.get("groupId"));
                } else {
                    throw new Exception("参数输入不正确");
                }
                systemDeploy.deleteStageByGroupId(groupId);

                hmap.put("code", "200");
            }
        } catch (Exception e) {
            hmap.put("code", "500");
            hmap.put("message", e.getMessage());
        }
        retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }

    @RequestMapping(value = "/delProject", produces = "application/json")
    public String delProject(@RequestParam Map map) {
        long projectId = 0;
        String retvalue = "";
        Map<String, Object> hmap = new HashMap<String, Object>();
        try {
            if (map != null) {
                if (map.get("projectId") != null && StringUtils.isNotEmpty((String) map.get("projectId"))) {
                    projectId = Long.parseLong((String) map.get("projectId"));
                } else {
                    throw new Exception("参数输入不正确");
                }
        /* 资源隔离，权限验证 */
                if (!userRoleRelImpl.verifyPurview("projectId", projectId)) {
                    return null;
                }
        /* 资源隔离，权限验证 Over */

                systemDeploy.deleteStageByProjectId(projectId);

                hmap.put("code", "200");
            }
        } catch (Exception e) {
            hmap.put("code", "500");
            hmap.put("message", e.getMessage());
        }
        retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }

    @RequestMapping(value = "/delBranch", produces = "application/json")
    public String delBranch(@RequestParam Map map) {
        long branchId = 0;
        String retvalue = "";
        Map<String, Object> hmap = new HashMap<String, Object>();
        try {
            if (map != null) {
                if (map.get("branchId") != null && StringUtils.isNotEmpty((String) map.get("branchId"))) {
                    branchId = Long.parseLong((String) map.get("branchId"));
                } else {
                    throw new Exception("参数输入不正确");
                }
        /* 资源隔离，权限验证 */
                if (!userRoleRelImpl.verifyPurview("branchId", branchId)) {
                    return null;
                }
        /* 资源隔离，权限验证 Over */
                AdPipeLineState adPipeLineState = stateDAO.qryEnvById(branchId);
                if (adPipeLineState.getBranchState() == 2) {
                    throw new Exception("该流水正在构建中不能删除");
                }
                systemDeploy.deleteBranchById(branchId);
                hmap.put("code", "200");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            hmap.put("code", "500");
            hmap.put("message", e.getMessage());
        }
        retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }

    /*
    * 校验分支代码仓库
    * */
    @RequestMapping(value = "/deployGitcheck", produces = "application/json")
    public String deployGitcheck(@RequestParam Map map, HttpServletRequest req) {
        String projectId = "";
        String checkPath = "";
        String retvalue = "";
        Map<String, Object> hmap = new HashMap<String, Object>();
        boolean flag = false;
        int jj = 0;
        try {
            if (map != null) {
                if (map.get("projectId") != null && StringUtils.isNotEmpty((String) map.get("projectId"))) {
                    projectId = (String) map.get("projectId");
                }
                if (map.get("checkPath") != null && StringUtils.isNotEmpty((String) map.get("checkPath"))) {
                    checkPath = (String) map.get("checkPath");
                }
                if (StringUtils.isNotEmpty(projectId) && StringUtils.isEmpty(checkPath)) {
                    throw new Exception("参数输入不正确");
                }
                String privateToken = (String) req.getSession().getAttribute("privateToken");
                jj = gitServiceImpl.gitCheck(checkPath, "", privateToken);
            }
            if (jj == CommConstants.GIT_CHECK.BRANCH_EXIST || jj == CommConstants.GIT_CHECK.CODESTORE_EXIST) {
                flag = true;
            }
            hmap.put("code", "200");
            hmap.put("isexits", flag);
        } catch (Exception e) {
            hmap.put("code", "500");
            hmap.put("isexits", flag);
        }
        retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }

    @RequestMapping(value = "/qryJenkins", produces = "application/json")
    public String qryJenkins(@RequestParam Map map) {
        String retvalue;
        Map<String, Object> retmap = new HashMap<String, Object>();
        try {
            List<AdJenkinsInfo> adJenkinsInfoList = systemDeploy.qryAllJenkins();
            Map<String, String> hmap = null;
            List<Map<String, String>> alist = new ArrayList<Map<String, String>>();
            if (adJenkinsInfoList != null) {
                for (AdJenkinsInfo adJenkinsInfo : adJenkinsInfoList) {
                    hmap = new HashMap<String, String>();
                    hmap.put("jkId", "" + adJenkinsInfo.getJenkinsId());
                    hmap.put("jkUrl", "" + adJenkinsInfo.getJenkinsUrl());
                    alist.add(hmap);
                }
            }
            retmap.put("jenkins", alist);
            retmap.put("code", "200");
        } catch (Exception e) {
            retmap.put("code", "500");
            retmap.put("message", e.getMessage());
            e.printStackTrace();
        }
        retvalue = JsonUtil.mapToJson(retmap);
        return retvalue;
    }

    @RequestMapping(value = "/qryBuildTools", produces = "application/json")
    public SysBuildToolResult qryBuildTools(@RequestParam Map map) {
        List<SysBuildTool> sysBuildToolList;
        SysBuildToolResult sysBuildToolResult = new SysBuildToolResult();
        if (map.get("compileTool") != null && StringUtils.isNotEmpty((String) map.get("compileTool"))) {
            sysBuildToolList = systemDeploy.qryAllBuildTools();
        } else {
            sysBuildToolList = systemDeploy.qryCompileTools();
        }
        sysBuildToolResult.setTools(sysBuildToolList);
        return sysBuildToolResult;
    }

    @RequestMapping(value = "/updateProjectCodestore", produces = "application/json")
    public CommonPojo updateProjectCodestore(@RequestParam Map map) throws Exception {
        long projectId;
        String codeStore;
        String gitProjectId;
        CommonPojo commonPojo = new CommonPojo();
        try {
            if (map.get("projectId") != null && StringUtils.isNotEmpty((String) map.get("projectId"))) {
                projectId = Long.parseLong((String) map.get("projectId"));
            } else {
                throw new Exception("参数输入不正确");
            }
            if (map.get("codeStore") != null && StringUtils.isNotEmpty((String) map.get("codeStore"))) {
                codeStore = (String) map.get("codeStore");
            } else {
                throw new Exception("参数输入不正确");
            }
            if (map.get("gitProjectId") != null && StringUtils.isNotEmpty((String) map.get("gitProjectId"))) {
                gitProjectId = (String) map.get("gitProjectId");
            } else {
                throw new Exception("参数输入不正确");
            }
            systemDeploy.updateProjectJobname(codeStore, projectId, gitProjectId);
        } catch (Exception e) {
            commonPojo.setRetCode("500");
            commonPojo.setRetMessage(e.getMessage());
        }
        return commonPojo;
    }

    /**
     * mark：**************改造方法****************
     *
     * @param proobj 项目信息
     * @return 结果
     */
    @RequestMapping(value = "/addReformSystemDeploy", produces = "application/json")
    public String addReformSystemDeploy(@RequestBody SysReformProjectPojoExt proobj) {
        String retvalue;                                                                                                //返回结果
        Map<String, Object> hmap = new HashMap<String, Object>();                                                                    //存储结果的map
        try {
            systemDeploy.addReformSystemDeploy(proobj);                                                                 //新建成功
            hmap.put("retCode", "200");
        } catch (Exception e) {
            hmap.put("retCode", "500");                                                                                    //存储失败信息
            hmap.put("message", e.getMessage());
            e.printStackTrace();
        }
        retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }

    /**
     * @param proobj 传递过来的对象
     * @return
     */
    @RequestMapping(value = "/updateSystemDeploy", produces = "application/json")
    public String updateSystemDeploy(@RequestBody SysReformProjectPojoExt proobj) {
        int branchType = 0;
        String retvalue = "";
        Map<String, Object> hmap = new HashMap<String, Object>();
        try {
            jenkins.updateReformSystemDeploy(proobj);
            hmap.put("retCode", "200");
        } catch (Exception e) {
            hmap.put("retCode", "500");
            hmap.put("message", e.getMessage());
            e.printStackTrace();
        }
        retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }

    @RequestMapping("/uploadjar")
    public CommonPojo uploadjar(@RequestParam("branch_id") String branch_id, @RequestParam(value = "file") MultipartFile file) throws IllegalStateException, IOException {
        CommonPojo commonPojo = new CommonPojo();
        //取得当前上传文件的文件名称
        String fileName = file.getOriginalFilename();
        if (!"".equals(fileName.trim())) {
            AdBranch adBranch = adbranchImpl.qryById(Long.parseLong(branch_id));
            if (adBranch == null) {
                commonPojo.setRetCode("500");
                commonPojo.setRetMessage("找不到对应的branch！");
                return commonPojo;
            }
            String ftpPath = adBranch.getOriginPath();
            AdJenkinsInfo adJenkinsInfo = adBranch.getAdJenkinsInfo();
            String jenkinsUrl = adJenkinsInfo.getJenkinsUrl();
            String serverUsername = adJenkinsInfo.getServerUsername();
            String serverPassword = adJenkinsInfo.getServerPassword();

            FTPClient ftpClient = new FTPClient();
            FileInputStream fis = null;

            try {
                //登陆ftp
                ftpClient.connect(jenkinsUrl);
                ftpClient.login(serverUsername, serverPassword);
                if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                    commonPojo.setRetCode("500");
                    commonPojo.setRetMessage("FTP登陆失败！");
                    return commonPojo;
                }

                //创建目录，要一级级创建
                String[] path = ftpPath.split("/");
                StringBuilder tmpPath = new StringBuilder("");
                for (int i = 0; i < path.length; i++) {
                    tmpPath.append("/").append(path[i]);
                    ftpClient.makeDirectory(tmpPath.toString());
                }

                fis = (FileInputStream) file.getInputStream();
                // 设置上传目录
                ftpClient.changeWorkingDirectory(ftpPath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                // 设置文件类型（二进制）
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                ftpClient.storeFile(fileName, fis);
                if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                    commonPojo.setRetCode("500");
                    commonPojo.setRetMessage("FTP上传失败！");
                    return commonPojo;
                }

                //默认写commit为0，否则后面不会自动上传到artifactory
                AdStage adStage1 = adStageDAO.qryStageByStep(Long.parseLong(branch_id), 1);
                if (adStage1.getCommitId() == null) {
                    adStage1.setCommitId("0");
                    adStage1.save();
                }
            } catch (IOException e) {
                e.printStackTrace();
                commonPojo.setRetCode("500");
                commonPojo.setRetMessage("FTP客户端出错！" + e.getMessage());
                return commonPojo;
            } finally {
                IOUtils.closeQuietly(fis);
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return commonPojo;
    }

    /**
     * @param map 传递过来的Cron表达式
     * @return
     * @authoer 刘兆祥
     */
    @RequestMapping(value = "/checkCron", produces = "application/json")
    public Pojo checkCron(@RequestParam Map map) {
        Pojo pojo = new CommonPojo();
        pojo.setRetMessage("false");
        try {
            if (systemDeploy.checkCron(map)) {
                pojo.setRetMessage("true");
                return pojo;
            }
        } catch (Exception e) {
            pojo.setRetCode("500");
            pojo.setRetMessage(e.getMessage());
            e.printStackTrace();
        }
        return pojo;
    }

    /**
     * @param map 传递过来的Cron表达式
     * @return
     * @authoer 刘兆祥
     */
    @RequestMapping(value = "/checkAll", produces = "application/json")
    public Pojo checkAll(@RequestParam Map map, HttpServletRequest req) {
        Pojo pojo = new CommonPojo();
        pojo.setRetMessage("false");
        try {
            if (gitServiceImpl.gitCheckPre(map, req) && systemDeploy.checkCron(map)) {
                pojo.setRetMessage("true");
            }
        } catch (Exception e) {
            pojo.setRetCode("500");
            pojo.setRetMessage(e.getMessage());
            e.printStackTrace();
        }
        return pojo;
    }

    //流水线复制接口
    @RequestMapping(value = "/copySystemDeploy", produces = "application/json")
    public DefaultPojo copySystemDeploy(@RequestBody BranchCopyPojoExt copyPojo, HttpServletRequest request) throws Exception {
        DefaultPojo pojo = new DefaultPojo();
        long srcBranchId = copyPojo.getSrcBranchId();
        String branchName = copyPojo.getBranchName();
        String branchDesc = copyPojo.getBranchDesc();
        String branchPath = copyPojo.getBranchPath();
        String branchType = copyPojo.getBranchType();
        String triggerBranch = copyPojo.getTriggerBranch();
        AdBranch adBranch = adbranchImpl.qryById(srcBranchId);
        if (adBranch == null) {
            throw new Exception("branchId不存在");
        }
        AdProject adProject = adBranch.getAdProject();
        if (adProject == null) {
            throw new Exception("project不存在");
        }

        //验证登陆人员是否有该项目权限
        if (!userRoleRelImpl.verifyPurview("projectId", adProject.getProjectId())) {
            throw new Exception("您没有该项目权限！");
        }

        //新建下载节点
        SysStagePojoExt downloadStage = new SysStagePojoExt();
        downloadStage.setStagecode("1");
        downloadStage.setShellCommand("git show --name-only");
        downloadStage.setCheck(true);
        downloadStage.setJkJobName("下载");
        SysStagePojoExt[] tmpStages = {downloadStage};

        //新建branch
        SysReformBranchPojoExt newBranch = new SysReformBranchPojoExt();
        newBranch.setBranchName(branchName);
        newBranch.setBranchDesc(branchDesc);
        newBranch.setBranchPath(branchPath);
        newBranch.setBranchType(branchType);
        newBranch.setTriggerBranch(triggerBranch);
        newBranch.setStages(tmpStages);
        SysReformBranchPojoExt[] tmpBranch = {newBranch};

        SysReformProjectPojoExt sysProjectPojoExt = new SysReformProjectPojoExt();
        sysProjectPojoExt.setBuildTool(adProject.getBuildTool());
        sysProjectPojoExt.setCompileTool(adProject.getCompileTool());
        sysProjectPojoExt.setCompileToolVersion(adProject.getCompileVersion());
        sysProjectPojoExt.setProjectId(Long.toString(adProject.getProjectId()));
        sysProjectPojoExt.setObj(tmpBranch);

        //调新增接口
        systemDeploy.addReformSystemDeploy(sysProjectPojoExt);

        //重新查询一遍
        sysProjectPojoExt = systemDeploy.qrySystemDeploy(adProject.getProjectId());
        SysReformBranchPojoExt[] branchs = sysProjectPojoExt.getObj();
        SysReformBranchPojoExt srcBranch = null;
        //获取源branch和目标branch
        for (int i = 0; i < branchs.length; i++) {
            SysReformBranchPojoExt branch = branchs[i];
            if (Long.toString(srcBranchId).equals(branch.getBranchId())) {
                srcBranch = branch;
            }
            if (branchName.equals(branch.getBranchName())) {
                newBranch = branch;
            }
        }

        String srcBranchName = srcBranch.getBranchName();
        //设置节点
        SysStagePojoExt[] srcStages = srcBranch.getStages();
        SysStagePojoExt[] newStages = newBranch.getStages();
        for (int i = 0; i < srcStages.length; i++) {
            SysStagePojoExt srcStage = srcStages[i];
            SysStagePojoExt newStage = null;
            //下载和未启用的节点跳过
            if (!"1".equals(srcStage.getStagecode()) && srcStage.isCheck()) {
                //在新的数组里找到这个节点
                for (int j = 0; j < newStages.length; j++) {
                    if (srcStage.getStagecode().equals(newStages[j].getStagecode())) {
                        newStage = newStages[j];
                        break;
                    }
                }

                //替换脚本内容
                newStage.setCheck(srcStage.isCheck());
                if (srcStage.getShellCommand() != null) {
                    newStage.setShellCommand(srcStage.getShellCommand().replaceAll(srcBranchName, branchName));
                }
            }
        }

        //设置branch
        Map<String, Boolean> buildFileTypes = srcBranch.getBuildFileTypes();
        StringBuffer buildFileType = new StringBuffer("");
        if (buildFileTypes != null) {
            for (Map.Entry<String, Boolean> typeMap : buildFileTypes.entrySet()) {
                if (typeMap.getValue()) {
                    if ("".equals(buildFileType.toString())) {
                        buildFileType.append(typeMap.getKey());
                    } else {
                        buildFileType.append(",").append(typeMap.getKey());
                    }
                }
            }
        }
        newBranch.setBuildFileType(buildFileType.toString());
        newBranch.setEnvId(srcBranch.getEnvId());
        if (srcBranch.getOriginPath() != null) {
            newBranch.setOriginPath(srcBranch.getOriginPath().replaceAll(srcBranchName, branchName));
        }
        newBranch.setStages(newStages);
        SysReformBranchPojoExt[] newBranchs = {newBranch};

        //设置pojo
        sysProjectPojoExt.setBuildTool(adProject.getBuildTool());
        sysProjectPojoExt.setCompileTool(adProject.getCompileTool());
        sysProjectPojoExt.setCompileToolVersion(adProject.getCompileVersion());
        sysProjectPojoExt.setProjectId(Long.toString(adProject.getProjectId()));
        sysProjectPojoExt.setObj(newBranchs);

        jenkins.updateReformSystemDeploy(sysProjectPojoExt);
        return pojo;
    }
}



