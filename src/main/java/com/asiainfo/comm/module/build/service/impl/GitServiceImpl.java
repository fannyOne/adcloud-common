package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.auth.sso.gitlib.api.GitlabAPI;
import com.asiainfo.auth.sso.gitlib.api.models.GitlabBranch;
import com.asiainfo.auth.sso.gitlib.api.models.GitlabProject;
import com.asiainfo.auth.sso.gitlib.api.models.GitlabSession;
import com.asiainfo.auth.sso.gitlib.api.models.GitlabTag;
import com.asiainfo.comm.common.pojo.pojoExt.AdDcosDeployDtlExt;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.util.SshUtil;
import com.asiainfo.comm.module.build.dao.impl.*;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosBranchRelateDAO;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosDeployInfoDAO;
import com.asiainfo.comm.module.deploy.dao.impl.AdVirtualBranchRelateDAO;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.avaje.ebean.SqlRow;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yry on
 * 2016/6/16 0016.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class GitServiceImpl {

    @Autowired
    AdGitCommitDAO gitCommitDAO;
    @Autowired
    AdBranchDAO envDAO;
    @Autowired
    AdStageDAO operationDAO;
    @Autowired
    AdPipeLineStateDAO stateDAO;
    @Autowired
    JenkinsImpl jenkinsService;
    @Value("${gitlab.server.url}")
    String gitUrl;
    @Value("${gitlab.username}")
    String gitUsername;
    @Value("${gitlab.password}")
    String gitPass;
    @Value("${gitlab.admin.token}")
    private String token;
    @Autowired
    AdProjectDAO adProjectDAO;
    @Autowired
    AdDockImagesDAO adDockImagesDAO;
    @Autowired
    AdBranchDAO adBranchDAO;
    @Autowired
    AdStageDAO adStageDAO;
    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;
    @Autowired
    AdGitCommitDAO adGitCommitDAO;
    @Autowired
    AdDcosDeployInfoDAO dcosDeployInfoDAO;
    @Autowired
    AdVirtualEnvironmentDAO virtualEnvironmentDAO;
    @Autowired
    AdProjectDeployPackageDAO adProjectDeployPackageDAO;
    @Autowired
    AdDcosBranchRelateDAO adDcosBranchRelateDAO;
    @Autowired
    AdVirtualBranchRelateDAO adVirtualBranchRelateDAO;
    @Autowired
    AdUserImpl adUserImpl;
    @Autowired
    AdJenkinsInfoImpl adJenkinsInfoImpl;

    public String gitHookBuild(JSONObject json, Map param) throws Exception {
        String ref = "";
        long envId = 0;
        int buildType = 1;
        if (param.get("envId") != null) {
            String envIdStr = (String) param.get("envId");
            if (!StringUtils.isEmpty(envIdStr)) {
                envId = Long.parseLong(envIdStr);
            }
            if (param.get("buildType") != null) {
                buildType = (int) param.get("buildType");
            }
            if (json != null) {
                if (json.containsKey("ref")) {
                } else {
                    JSONObject jsonobject = json.getJSONObject("user");
                    JSONObject object = json
                        .getJSONObject("object_attributes");
                    if (object != null) {
                        JSONObject lastObject = object
                            .getJSONObject("last_commit");
                    }
                }
            }

        /* 资源隔离，权限验证 */
            if (!userRoleRelImpl.verifyPurview("branchId", envId)) {
                return null;
            }
        /* 资源隔离，权限验证 Over */

            //调用Jenkins
            if (envId != 0) {
                AdBranch env = envDAO.getEnvById(envId);
                AdPipeLineState envState = stateDAO.qryByEnvIdBuildType(envId, buildType);
                if (env != null) {
                    log.error("******************************环境编号***********************", env.getBranchId());
                    List<AdStage> opts = operationDAO.QryAdOperationByEnvIdType(envId);
                    if (opts != null && opts.size() > 0 && envState.getBranchState() != 2) {
                    }
                }
            }
        }
        return "success";
    }


    public int GitCheck(String projectName, String branchName, String gitServer) throws Exception {
        GitlabProject exitProjectName = null;
        if (StringUtils.isNotEmpty(gitServer)) {
            gitUrl = gitServer.substring(gitServer.indexOf("@") + 1, gitServer.indexOf(":"));
            gitUrl = "http://" + gitUrl;
        }
        GitlabSession gitlabSession = GitlabAPI.connect(gitUrl, gitUsername, gitPass);
        String token = gitlabSession.getPrivateToken();
        GitlabAPI gitlabAPI = new GitlabAPI(gitUrl, token);
        List<GitlabProject> gitlabProjects = gitlabAPI.getProjects();
        int flag = CommConstants.GIT_CHECK.CODESTORE_NO;
        for (GitlabProject gitlabProject : gitlabProjects) {
            if ((gitlabProject.getName()).equals(projectName)) {
                exitProjectName = gitlabProject;
                flag = CommConstants.GIT_CHECK.CODESTORE_EXIST;
                break;
            }
        }
        if (StringUtils.isNotEmpty(branchName)) {
            GitlabBranch gitlabBranch = null;
            if (exitProjectName != null) {
                try {
                    gitlabBranch = gitlabAPI.getBranch(exitProjectName, branchName);
                } catch (Exception e) {
                    flag = CommConstants.GIT_CHECK.BRANCH_NO;
                }
            } else {
                flag = CommConstants.GIT_CHECK.CODESTORE_EXIST;
            }
            if (gitlabBranch == null) {
                flag = CommConstants.GIT_CHECK.BRANCH_NO;
            } else {
                flag = CommConstants.GIT_CHECK.CODESTORE_EXIST;
            }
        }
        return flag;
    }

    public List<GitlabTag> qryGitProjectTag(String gitProjectId, String projectName) {
        List<GitlabProject> gitlabProjects = null;
        GitlabProject labProject = null;
        List<GitlabTag> gitlabTags = null;
        GitlabAPI gitlabAPI = new GitlabAPI(gitUrl, token);
        if (StringUtils.isEmpty(gitProjectId)) {
            try {
                gitlabProjects = gitlabAPI.getAllProjects();
                for (GitlabProject gitlabProject : gitlabProjects) {
                    if (gitlabProject.getName().equals(projectName)) {
                        labProject = gitlabProject;
                        continue;
                    }
                    gitlabTags = gitlabAPI.getTags(labProject);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            try {
                gitlabTags = gitlabAPI.getTags(gitProjectId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gitlabTags;
    }

    public Map<String, Object> qryProjectTag(long projectId) throws Exception {
        String ret = "";
        AdProject adProject;
        String codeStore;
        int status = 0;
        Map<String, Object> tagMap;
        List<Map<String, Object>> tagList;
        List<GitlabTag> gitlabTagList = null;
        int has_image = 0;
        List<String> running_on = new ArrayList<String>();
        Map<String, Object> retMap = new HashMap<String, Object>();
        adProject = adProjectDAO.getSystemById(projectId);
        running_on.add("5");
        running_on.add("6");
        long begin_time = System.currentTimeMillis();
        if (adProject != null) {
            codeStore = adProject.getCodeStore();
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(codeStore)) {
                codeStore = codeStore.substring(codeStore.lastIndexOf("/") + 1, codeStore.lastIndexOf("."));
            } else {
                throw new Exception("代码仓库未配置");
            }
            gitlabTagList = qryGitProjectTag(adProject.getGitProjectid(), codeStore);
            List<AdDockImages> adDockImagesList = adDockImagesDAO.getDockImagsByProjectName(codeStore);
            if (gitlabTagList != null) {
                tagList = new ArrayList<Map<String, Object>>();
                for (GitlabTag gitlabTag : gitlabTagList) {
                    status = 0;
                    has_image = 0;
                    tagMap = new HashMap<String, Object>();
                    if (adDockImagesList != null) {
                        for (AdDockImages adDockImages : adDockImagesList) {
                            if (gitlabTag.getName().equals(adDockImages.getTag())) {
                                status = adDockImages.getImageStatus();
                                has_image = adDockImages.getHasImage() == null ? 0 : adDockImages.getHasImage();
                                continue;
                            }
                        }
                    }
                    tagMap.put("name", gitlabTag.getName());
                    tagMap.put("image_build_status", "" + status);
                    if (has_image == 1) {
                        tagMap.put("has_image", "true");
                    } else {
                        tagMap.put("has_image", "false");
                    }
                    tagMap.put("running_on", running_on);
                    tagList.add(tagMap);
                    retMap.put("tags", tagList);
                }
            } else {
                throw new Exception("获取git仓库失败");
            }
            List<AdBranch> adBranchList = adBranchDAO.getEnvsBySysId(projectId);
            retMap.put("branchs", adBranchList);
        } else {
            throw new Exception("project获取失败");
        }
        retMap.put("projectname", codeStore);
        retMap.put("projectid", "" + projectId);
        long end_time = System.currentTimeMillis();
        if (log.isErrorEnabled()) {
            log.error("************time=" + (end_time - begin_time));
        }
        return retMap;
    }

    public String getInitTagName(long branchId) throws Exception {
        String tagName = "";
        String end = "";
        AdBranch adBranch = adBranchDAO.qryBranchByid(branchId);
        if (adBranch != null) {
            end = CommConstants.qryNameEnd(adBranch.getBranchType() + "");
            tagName = adBranch.getBranchName() + "_" + DateConvertUtils.date2String(new Date(), "yyyyMMddHHmm") + "_" + end;
        }
        return tagName;
    }


    public void addProjectTag(long branchId, String tagName) throws Exception {
        String tagMess = "";
        String tagDesc = "";
        String gitProjectId = "";
        String commitId = "";
        String end = "";
        AdBranch adBranch = adBranchDAO.qryBranchByid(branchId);
        if (adBranch != null) {
            List<AdStage> adStageList = adStageDAO.QryValidAdOperationByEnvId(branchId);
            for (AdStage adStage : adStageList) {
                if (adStage.getStageCode() == 1) {
                    commitId = adStage.getCommitId();
                    break;
                }
            }
            if (StringUtils.isNotEmpty(commitId)) {
                end = CommConstants.qryNameEnd(adBranch.getBranchType() + "");
                gitProjectId = adBranch.getAdProject().getGitProjectid();
                GitlabSession gitlabSession = GitlabAPI.connect(gitUrl, gitUsername, gitPass);//branchname_20160802_dev_commitid
                String token = gitlabSession.getPrivateToken();
                GitlabAPI gitlabAPI = new GitlabAPI(gitUrl, token);
                if (StringUtils.isEmpty(tagName)) {
                    tagName = adBranch.getBranchName() + "_" + DateConvertUtils.date2String(new Date(), "yyyyMMddHHmm") + "_" + end;//+ "_" + commitId;
                } else {
                    List<GitlabTag> gitlabTagList = gitlabAPI.getTags(gitProjectId);
                    for (GitlabTag gitlabTag : gitlabTagList) {
                        if (gitlabTag.getName().equals(tagName)) {
                            throw new Exception("tag名称已经存在,请重新输入tag名称");
                        }
                    }
                }

                gitlabAPI.addTag(gitProjectId, tagName, "master", tagDesc, tagName);
                AdProjectGitTags adProjectGitTags = new AdProjectGitTags();
                adProjectGitTags.setProjectId(adBranch.getAdProject().getProjectId());
                adProjectGitTags.setTagName(tagName);
                adProjectGitTags.setCommitId(commitId);
                adProjectGitTags.setCreateDate(new Date());
                adProjectGitTags.setAdBranch(adBranch);
                adProjectGitTags.setBranchType(adBranch.getBranchType());
                double version;
                if (adBranch.getVersion() != null && adBranch.getVersion() != 0) {
                    version = (adBranch.getVersion() + 1) / 100.0;
                    adBranch.setVersion(adBranch.getVersion() + 1);
                    adProjectGitTags.setVersion("v" + version);
                } else {
                    adBranch.setVersion(100l);
                    adProjectGitTags.setVersion("v1.0");
                }
                adBranch.save();
                adProjectGitTags.save();
            } else {
                throw new Exception("commit为空打tag失败");
            }
        }

    }

    public List<Map<String, String>> qryProjectVersion(long projectId, int buildType) throws Exception {
        List<AdProjectGitTags> adProjectGitTagsList = adGitCommitDAO.qryProjectVersion(projectId);
        Map<String, String> hmap;
        List<Map<String, String>> alist = new ArrayList<Map<String, String>>();
        packageProjectTagsInfo(adProjectGitTagsList, alist);
        return alist;
    }

    public List<AdDcosDeployDtlExt> qryDcosDeployDtl(Long envId, String envType) throws Exception {
        List<AdDcosDeployDtlExt> adDcosDeployDtlExtList = null;
        if (envType.equals("dcos")) {
            List<AdDcosDeployDtl> adDcosDeployDtlList = dcosDeployInfoDAO.qryDcosDeployDtlByDcosDeployId(envId);
            if (adDcosDeployDtlList != null) {
                adDcosDeployDtlExtList = new ArrayList<AdDcosDeployDtlExt>();
                AdDcosDeployDtlExt adDcosDeployDtlExt;
                for (AdDcosDeployDtl adDcosDeployDtl : adDcosDeployDtlList) {
                    adDcosDeployDtlExt = new AdDcosDeployDtlExt();
                    adDcosDeployDtlExt.setAppid(adDcosDeployDtl.getAppid());
                    adDcosDeployDtlExt.setBranchId(adDcosDeployDtl.getBranchId());
                    adDcosDeployDtlExt.setPackageName(adDcosDeployDtl.getPackageName());
                    adDcosDeployDtlExt.setPriorityNum(adDcosDeployDtl.getPriorityNum());
                    adDcosDeployDtlExt.setCheck(false);
                    adDcosDeployDtlExtList.add(adDcosDeployDtlExt);
                }
            }
        }
        return adDcosDeployDtlExtList;
    }

    public List<Map<String, Object>> qryProjectVersion(long projectId, int buildType, Long envId, String envType) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuffer sb = new StringBuffer();
        if (envType.equals("dcos")) {
            List<AdDcosBranchRelate> adDcosBranchRelateList = adDcosBranchRelateDAO.qryBranchsByEnvId(envId);
            for (AdDcosBranchRelate adDcosBranchRelate : adDcosBranchRelateList) {
                sb.append(adDcosBranchRelate.getAdBranch().getBranchId() + ",");
            }
        } else {
            List<AdVirtualBranchRelate> adVirtualBranchRelateList = adVirtualBranchRelateDAO.qryBranchsByEnvId(envId);
            for (AdVirtualBranchRelate adVirtualBranchRelate : adVirtualBranchRelateList) {
                sb.append(adVirtualBranchRelate.getAdBranch().getBranchId() + ",");
            }
        }
        String branchId = sb.toString();
        Map<String, Object> hmap;
        List<Map<String, Object>> alist = new ArrayList<Map<String, Object>>();
        if (branchId.indexOf(",") > 0) {
            branchId = branchId.substring(0, branchId.length() - 1);
            List<SqlRow> adProjectDeployPackageList = adProjectDeployPackageDAO.qryByBranchs(branchId);
            String packagePath = "";
            String[] packageName;
            String branchName = "";
            String tagName = "";
            for (SqlRow sqlRow : adProjectDeployPackageList) {
                hmap = new HashMap<String, Object>();
                AdBranch adBranch = adBranchDAO.qryBranchByid(sqlRow.getLong("branch_id"));
                branchName = adBranch.getBranchName();
                packagePath = sqlRow.get("package_path") + "";
                if (packagePath != null) {
                    packageName = packagePath.split("/");
                    if (packageName.length > 0) {
                        tagName = branchName + "_" + packageName[packageName.length - 3] + "_" + packageName[packageName.length - 1];
                    }
                }
                hmap.put("version", sqlRow.get("commit_id") + "");
                hmap.put("tagName", tagName);
                hmap.put("createDate", "" + sdf.format(sqlRow.get("create_date")));
                hmap.put("commitId", sqlRow.get("commit_id") + "");
                hmap.put("protagId", "" + sqlRow.get("package_id"));
                hmap.put("branchName", adBranch.getBranchDesc());
                hmap.put("branchId", adBranch.getBranchId() + "");
                hmap.put("packagePath", packagePath.split("http://")[1]);
                alist.add(hmap);
            }
        }
        return alist;
    }

    public List<Map<String, Object>> qryProjectVersionDesc(Long envId, String envType) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuffer sb = new StringBuffer();
        if (envType.equals("dcos")) {
            List<AdDcosBranchRelate> adDcosBranchRelateList = adDcosBranchRelateDAO.qryBranchsByEnvId(envId);
            for (AdDcosBranchRelate adDcosBranchRelate : adDcosBranchRelateList) {
                sb.append(adDcosBranchRelate.getAdBranch().getBranchId() + ",");
            }
        } else {
            List<AdVirtualBranchRelate> adVirtualBranchRelateList = adVirtualBranchRelateDAO.qryBranchsByEnvId(envId);
            for (AdVirtualBranchRelate adVirtualBranchRelate : adVirtualBranchRelateList) {
                sb.append(adVirtualBranchRelate.getAdBranch().getBranchId() + ",");
            }
        }
        String branchId = sb.toString();
        Map<String, Object> hmap;
        List<Map<String, Object>> alist = new ArrayList<Map<String, Object>>();
        if (branchId.indexOf(",") > 0) {
            branchId = branchId.substring(0, branchId.length() - 1);
            List<SqlRow> adProjectDeployPackageList = adProjectDeployPackageDAO.qryByBranchsDesc(branchId);
            String packagePath = "";
            String[] packageName;
            String branchName = "";
            String tagName = "";
            for (SqlRow sqlRow : adProjectDeployPackageList) {
                hmap = new HashMap<>();
                AdBranch adBranch = adBranchDAO.qryBranchByid(sqlRow.getLong("branch_id"));
                branchName = adBranch.getBranchName();
                packagePath = sqlRow.get("package_path") + "";
                if (packagePath != null) {
                    packageName = packagePath.split("/");
                    if (packageName.length > 0) {
                        tagName = branchName + "_" + packageName[packageName.length - 3] + "_" + packageName[packageName.length - 1];
                    }
                }
                hmap.put("version", sqlRow.get("commit_id") + "");
                hmap.put("tagName", tagName);
                hmap.put("createDate", "" + sdf.format(sqlRow.get("create_date")));
                hmap.put("commitId", sqlRow.get("commit_id") + "");
                hmap.put("protagId", "" + sqlRow.get("package_id"));
                hmap.put("branchName", adBranch.getBranchDesc());
                hmap.put("branchId", adBranch.getBranchId() + "");
                hmap.put("packagePath", packagePath.split("http://")[1]);
                alist.add(hmap);
            }
        }
        return alist;
    }

    public Object qryProjectVersionByBranch(long branchId) {
        List<AdProjectGitTags> adProjectGitTagsList = adGitCommitDAO.qryProjectVersionByBranch(branchId);
        Map<String, String> hmap;
        List<Map<String, String>> alist = new ArrayList<Map<String, String>>();
        packageProjectTagsInfo(adProjectGitTagsList, alist);
        return alist;
    }

    private void packageProjectTagsInfo(List<AdProjectGitTags> adProjectGitTagsList, List<Map<String, String>> alist) {
        Map<String, String> hmap;
        if (adProjectGitTagsList != null) {
            for (AdProjectGitTags adProjectGitTag : adProjectGitTagsList) {
                hmap = new HashMap<String, String>();
                hmap.put("version", adProjectGitTag.getVersion());
                hmap.put("tagName", adProjectGitTag.getTagName());
                hmap.put("createDate", "" + adProjectGitTag.getCreateDate());
                hmap.put("commitId", adProjectGitTag.getCommitId());
                hmap.put("protagId", "" + adProjectGitTag.getProTagId());
                alist.add(hmap);
            }
        }
    }

    public boolean gitCheckPre(Map map, HttpServletRequest req) {
        String url = "", branch = "";
        boolean flag = false;
        Map retMap = new HashMap<>();
        try {
            if (map != null && map.get("giturl") != null) {
                url = (String) map.get("giturl");
            }
            if (map != null && map.get("branch") != null) {
                branch = (String) map.get("branch");
                branch = branch.substring(branch.indexOf("/") + 1);
            }
            String privateToken = (String) req.getSession().getAttribute("privateToken");
            int jj = gitCheck(url, branch, privateToken);
            if (jj == CommConstants.GIT_CHECK.BRANCH_EXIST) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public int gitCheck(String path, String branchName, String token) throws Exception {
        GitlabAPI gitlabAPI = new GitlabAPI(gitUrl, token);
        List<GitlabProject> gitlabProjects = gitlabAPI.getProjects();
        int flag = CommConstants.GIT_CHECK.CODESTORE_NO;
//        path = replaceUrl(path);
        for (GitlabProject gitlabProject : gitlabProjects) {
            if (path.equals(gitlabProject.getHttpUrl()) || path.equals(gitlabProject.getSshUrl())) {
                flag = CommConstants.GIT_CHECK.CODESTORE_EXIST;
                if (StringUtils.isNotEmpty(branchName)) {
                    flag = CommConstants.GIT_CHECK.BRANCH_NO;
                    GitlabBranch gitlabBranch = gitlabAPI.getBranch(gitlabProject, branchName);
                    if (null != gitlabBranch) {
                        flag = CommConstants.GIT_CHECK.BRANCH_EXIST;
                    }
                }
                break;
            }
        }
        return flag;
    }


    public List<Map<String, String>> qryGitAllProjects(String token) {
        List<GitlabProject> gitlabProjects = null;
        List<Map<String, String>> retgitProject = new ArrayList<Map<String, String>>();
        Map<String, String> retMap;
        GitlabAPI gitlabAPI = new GitlabAPI(gitUrl, token);
        try {
            gitlabProjects = gitlabAPI.getProjects();
            for (GitlabProject gitlabProject : gitlabProjects) {
                retMap = new HashMap<String, String>();
                retMap.put("id", "" + gitlabProject.getId());
                retMap.put("sshurl", "" + gitlabProject.getSshUrl());
                retMap.put("httpurl", "" + gitlabProject.getHttpUrl());
                retgitProject.add(retMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retgitProject;
    }

    public GitlabProject createUserProject(HttpServletRequest req,String token, String projectName) throws IOException {
        GitlabAPI gitlabAPI = new GitlabAPI(gitUrl, token);
        GitlabProject gitlabProject;
        try {
            gitlabProject = gitlabAPI.createProject(projectName, null, projectName, true, true, true, true, true, null, 0, null);
        } catch (IOException e) {
            throw new IOException("创建仓库失败，请确认名称不重复且仓库数量未超过限制");
        }
        String userName = (String)req.getSession().getAttribute("username");
        AdUser adUser = adUserImpl.qryByName(userName);
        String pwd = adUser.getPassword();
        AdJenkinsInfo adJenkinsInfo = adJenkinsInfoImpl.qryByJkId(1L);
        String gitUrl = gitlabProject.getHttpUrl().split("http://")[0] + userName + ":" + pwd + "@"+gitlabProject.getHttpUrl().split("http://")[1];
        SshUtil sshUtil = new SshUtil(adJenkinsInfo.getJenkinsUrl(), adJenkinsInfo.getServerUsername(), adJenkinsInfo.getServerPassword(), "utf-8");
        String cmd = "cd /app/aideploy/sbin;  sh initRepository.sh '"
            + "http://" + gitUrl + "'";
        sshUtil.exec(cmd);
        gitlabAPI.protectBranch(gitlabProject, "dev");
        gitlabAPI.protectBranch(gitlabProject, "qa");
        gitlabAPI.protectBranch(gitlabProject, "master");
        return gitlabProject;
    }

    /*******************************************
     * 改造方法
     **********************************************/
    public List<Map<String, Object>> qryReformProjectVersion(long projectId, int buildType, Long envId, String envType) throws Exception {
        Long branchId = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (envType.equals("dcos")) {
            AdDcosDeployInfo info = dcosDeployInfoDAO.qryDcosDeployInfoById(envId);
            if (info != null) {
                branchId = info.getAdBranch().getBranchId();

            }
        } else {
            AdVirtualEnvironment info = virtualEnvironmentDAO.qryById(envId);
            if (info != null) {
                branchId = info.getAdBranch().getBranchId();
            }
        }
        Map<String, Object> hmap;
        List<Map<String, Object>> alist = new ArrayList<Map<String, Object>>();
        if (branchId != null) {
            List<AdProjectDeployPackage> adProjectDeployPackageList = adProjectDeployPackageDAO.qryByBranchId(branchId);
            String packagePath = "";
            String[] packageName;
            String branchName = "";
            String tagName = "";
            if (adProjectDeployPackageList != null) {
                for (AdProjectDeployPackage adProjectDeployPackage : adProjectDeployPackageList) {
                    hmap = new HashMap<String, Object>();
                    branchName = adProjectDeployPackage.getAdBranch().getBranchName();
                    packagePath = adProjectDeployPackage.getPackagePath();
                    if (packagePath != null) {
                        packageName = packagePath.split("/");
                        if (packageName.length > 0) {
                            tagName = branchName + "_" + packageName[packageName.length - 3] + "_" + packageName[packageName.length - 1];
                        }
                    }
                    hmap.put("version", adProjectDeployPackage.getCommitId());
                    hmap.put("tagName", tagName);
                    hmap.put("createDate", "" + sdf.format(adProjectDeployPackage.getCreateDate()));
                    hmap.put("commitId", adProjectDeployPackage.getCommitId());
                    hmap.put("protagId", "" + adProjectDeployPackage.getPackageId());
                    hmap.put("branchName", adProjectDeployPackage.getAdBranch().getBranchDesc());
                    alist.add(hmap);
                }
            }
        }
        return alist;
    }
}
