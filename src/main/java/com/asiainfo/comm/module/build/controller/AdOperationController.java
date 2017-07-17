package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.common.pojo.dataModel.BranchInfoPara;
import com.asiainfo.comm.common.pojo.pojoExt.AdFastenSignPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.AdFastenSignPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.AdUserPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.FlagPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.OperationNowPojo;
import com.asiainfo.comm.module.build.dao.impl.AdVirtualEnvironmentDAO;
import com.asiainfo.comm.module.build.service.impl.*;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.comm.module.deploy.service.impl.DockerInfoQryImpl;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdFastenSign;
import com.asiainfo.comm.module.models.AdStage;
import com.asiainfo.comm.module.models.AdVirtualEnvironment;
import com.asiainfo.comm.module.role.controller.BaseController;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.asiainfo.util.JsonUtil;
import com.asiainfo.util.JsonpUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.asiainfo.util.StringUtil.ADEncrypt;

/**
 * Created by yangry on 2016/6/16 0016.
 * ！需要验证资源隔离的路由请写到对应的Controller中 ！
 * ！不要继续写在该文件下 ！
 */
@RestController
@lombok.extern.slf4j.Slf4j
public class AdOperationController extends BaseController {
    @Autowired
    private AdOperationImpl optService;
    @Autowired
    private ProjectStateImpl projectState;
    @Autowired
    private AdStageLogDtlImpl envBuildLogDtl;
    @Autowired
    private AdStageImpl stageImpl;
    @Autowired
    private AdFastenSignImpl fastenSignImpl;
    @Autowired
    private AdBuildLogImpl adBuildLog;
    @Autowired
    private AdStaticDataImpl bsStaticDataImpl;
    @Autowired
    private AdBranchImpl branchImpl;
    @Value("${pagesize}")
    private int pageSize;
    @Autowired
    private DockerInfoQryImpl dockerInfoQry;
    @Autowired
    private AdUserRoleRelImpl userRoleRelImpl;
    //    @Autowired
//    private AdDevSubTaskImpl devSubTaskImpl;
    @Autowired
    private AdUserImpl adUserImpl;
    @Autowired
    private AdTreeDataImpl treeDataImpl;

    @Autowired
    private AdVirtualEnvironmentDAO adVirtualEnvironmentDAO;

    @RequestMapping(value = "/opts", produces = "application/json")
    public OperationNowPojo opts(@RequestParam long projectId) throws Exception {
        /* 资源隔离，权限验证 Over */
        return optService.qryPips(projectId);
    }

    @RequestMapping(value = "/projectOpts", produces = "application/json")
    public OperationNowPojo projectOpts(@RequestParam long projectId) throws Exception {
        if (!userRoleRelImpl.verifyPurview("projectId", projectId)) {
            return null;
        }
        return optService.qryProjectOpts(projectId);
    }

    @RequestMapping(value = "/branchOpt", produces = "application/json")
    public OperationNowPojo branchOpt(@RequestParam long branchId) throws Exception {
        if (!userRoleRelImpl.verifyPurview("branchId", branchId)) {
            return null;
        }
        return optService.qryBranchOpts(branchId);
    }


    @RequestMapping(value = "/fivebuild", produces = "application/json")
    public String fivebuild(@RequestParam Map map) throws IOException {
        long begin_time = System.currentTimeMillis();
        long projectId = map.get("projectId") != null ? Long.valueOf((String) map.get("projectId")) : 0;
        if (!userRoleRelImpl.verifyPurview("projectId", projectId)) {
            return null;
        }

        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest();
        HttpSession httpSession = httpServletRequest.getSession();
        String username = (String) httpSession.getAttribute("username");
        log.error("this is the fivebuild server for " + username);

        Map hmap = projectState.qryFiveBuildResult(projectId);
        String ret = JsonUtil.mapToJson(hmap);
        long end_time = System.currentTimeMillis();
        System.out.print("***************" + (end_time - begin_time));
        return ret;
    }

    @RequestMapping(value = "/buildlogdtl", produces = "application/json")
    public String buildlogdtl(@RequestParam Map map) throws IOException {
        long begin_time = System.currentTimeMillis();
        long envId = map.get("envId") != null ? Long.valueOf((String) map.get("envId")) : 0;
        long seqId = map.get("seqId") != null ? Long.valueOf((String) map.get("seqId")) : 0;
        if (!userRoleRelImpl.verifyPurview("branchId", envId)) {
            return null;
        }

        Map hmap = envBuildLogDtl.qryStageLogDtl(envId, seqId);
        String ret = JsonUtil.mapToJson(hmap);
        long end_time = System.currentTimeMillis();
        System.out.print("***************" + (end_time - begin_time));
        return ret;
    }

    @RequestMapping(value = "/stageLogdtl", produces = "application/json")
    public String stageLogdtl(@RequestParam Map map) throws IOException {
        long begin_time = System.currentTimeMillis();
        Map hmap = adBuildLog.qryStageLogList(map);
        String ret = JsonUtil.mapToJson(hmap);
        long end_time = System.currentTimeMillis();
        System.out.print("***************" + (end_time - begin_time));
        return ret;
    }

    @RequestMapping(value = "/queryStages", produces = "application/json")
    public String queryStages(@RequestParam Map map) throws IOException {
        String branchId = "";
        if (map != null) {
            branchId = (String) map.get("branchId");
        }
        Map<String, Object> retMap = new HashMap<String, Object>();
        Map<String, String> staticdataMap;
        List<Map<String, String>> staticdataList = new ArrayList<Map<String, String>>();
        if (StringUtils.isNotEmpty(branchId)) {

        /* 资源隔离，权限验证 */
            if (!userRoleRelImpl.verifyPurview("branchId", Long.valueOf(branchId))) {
                return null;
            }
        /* 资源隔离，权限验证 Over */

            List<AdStage> adStages = stageImpl.qryStageList(Long.valueOf(branchId));
            Map bsStaticDatas = bsStaticDataImpl.qryStaticDatas("BUILDER_TYPE");
            if (bsStaticDatas != null && adStages != null) {
                for (AdStage adStage : adStages) {
                    staticdataMap = new HashMap<String, String>();
                    staticdataMap.put("value", "" + adStage.getStageId());
                    staticdataMap.put("name", "" + bsStaticDatas.get("" + adStage.getStageCode()));
                    staticdataList.add(staticdataMap);
                }
            }
        }
        retMap.put("states", staticdataList);
        String ret = JsonUtil.mapToJson(retMap);
        return ret;
    }

    @RequestMapping(value = "/qryAllBranch", produces = "application/json")
    public List<AdBranch> qryAllBranch() {
        return branchImpl.qryAllBranch();
    }

    @RequestMapping(value = "/qryAllBranchInfo", produces = "application/json")
    public String qryAllBranchInfo(@RequestParam Map map) {
        String ret = "";
        String projects = "";
        int pageNum = 0;
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            BranchInfoPara branchInfoPara = new BranchInfoPara(map, projects, pageNum).invoke();
            pageNum = branchInfoPara.getPageNum();
            projects = branchInfoPara.getProjects();
            if (pageNum > 0) {
                pageNum = pageNum - 1;
            }
            String[] projectArray = projects.split(",");
        /* 资源隔离，权限验证 */
            if (!userRoleRelImpl.verifyPurview("projectId", projectArray)) {
                return null;
            }
        /* 资源隔离，权限验证 Over */

            List<AdVirtualEnvironment> adVirtualEnvironmentList = adVirtualEnvironmentDAO.getAllVmEnv(pageNum, pageSize, projectArray);
            long count = adVirtualEnvironmentDAO.getVmEnvCount(projectArray);
            if (adVirtualEnvironmentList != null) {
                for (AdVirtualEnvironment environment : adVirtualEnvironmentList) {
                    environment.setServerPassword(ADEncrypt(8));
                }
                retMap.put("env", adVirtualEnvironmentList);
            }
            retMap.put("total", count);
        } catch (Exception e) {
            retMap.put("code", "500");
            retMap.put("m", e.getMessage());
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }

    @RequestMapping(value = "/qryBranchByProject", produces = "application/json")
    public List<AdBranch> qryBranchByProject(String projectId) throws IOException {
        if (!StringUtils.isNotEmpty(projectId)) {
            return null;
        }
        String[] projectIds = projectId.split(",");
        if (!userRoleRelImpl.verifyPurview("projectId", projectIds)) {
            return null;
        }
        return branchImpl.qryBranchByProjects(projectIds);
    }

    @RequestMapping(value = "/envRun", produces = "application/json")
    public String envRun(@RequestParam Map map) {
        String tag = "";
        String branchId = "0";
        String operType = "";
        String projectName = "";
        Map hmap = new HashMap<String, String>();
        try {
            if (map != null) {
                if (map.get("projectName") != null) {
                    projectName = (String) map.get("projectName");
                } else {
                    throw new Exception("项目名不正确");
                }
                if (map.get("tag") != null) {
                    tag = (String) map.get("tag");
                } else {
                    throw new Exception("标签不正确");
                }
                if (map.get("branchid") != null) {
                    branchId = (String) map.get("branchid");
                } else {
                    throw new Exception("分支不正确");
                }
                if (map.get("operType") != null) {
                    operType = (String) map.get("operType");
                } else {
                    throw new Exception("opetype不正确");
                }
            }

            if (!userRoleRelImpl.verifyPurview("branchId", Long.parseLong(branchId))) {
                return null;
            }

            int startflag = 0;
            int stopflag = 0;
            if (("1").equals(operType)) {//启动
                startflag = dockerInfoQry.startContainers(projectName + ":" + tag, Integer.parseInt(branchId));
            }
            if (("2").equals(operType)) {//暂停
                stopflag = dockerInfoQry.stopContainers(projectName + ":" + tag, Integer.parseInt(branchId));
            }
            if (("1").equals(operType) && startflag == 1) {
                throw new Exception("启动失败");
            }
            if (("2").equals(operType) && stopflag == 1) {
                throw new Exception("暂停失败");
            }
            hmap.put("code", "200");
        } catch (Exception e) {
            hmap.put("code", "500");
            hmap.put("message", e.getMessage());
        }
        String ret = JsonUtil.mapToJson(hmap);
        return ret;
    }

    @RequestMapping(value = "/qryStageLogDtl", produces = "application/json")
    public String qryStageLogDtl(@RequestParam Map map) {
        long begin_time = System.currentTimeMillis();
        Map hmap = new HashMap<>();
        long branchId, stageId;
        long startSize = -1;
        long jenkinsnum = 0;
        try {
            if (map != null) {
                if (map.containsKey("branchId") && StringUtils.isNotEmpty((String) map.get("branchId"))) {
                    branchId = Long.valueOf((String) map.get("branchId"));
                    if (!userRoleRelImpl.verifyPurview("branchId", branchId)) {
                        return null;
                    }
                } else {
                    throw new Exception("分支编号不正确");
                }
                if (map.containsKey("stageId") && StringUtils.isNotEmpty((String) map.get("stageId"))) {
                    stageId = Long.valueOf((String) map.get("stageId"));
                } else {
                    throw new Exception("节点编号不正确");
                }
                if (map.containsKey("startSize") && StringUtils.isNotEmpty((String) map.get("startSize"))) {
                    startSize = Long.valueOf((String) map.get("startSize"));
                }
                if (map.containsKey("jenkinsNum") && StringUtils.isNotEmpty((String) map.get("jenkinsNum"))) {
                    jenkinsnum = Long.valueOf((String) map.get("jenkinsNum"));
                }
                System.out.println("*********startSize=" + startSize);
                hmap = optService.qryStageLogDtl(branchId, stageId, startSize, jenkinsnum);
            }
        } catch (Exception e) {
            e.printStackTrace();
            hmap.put("state", "fail");
            hmap.put("message", e.getMessage());
        }
        String ret = JsonUtil.mapToJson(hmap);
        long end_time = System.currentTimeMillis();
        System.out.print("***************" + (end_time - begin_time));
        return ret;
    }

    @RequestMapping(value = "/stageLogUser", produces = "application/json")
    public AdUserPojo stageLogUser(String projectId) throws IOException {
        long begin_time = System.currentTimeMillis();
        AdUserPojo pojo = adUserImpl.qryStageLogUser(projectId);
        long end_time = System.currentTimeMillis();
        System.out.print("***************" + (end_time - begin_time));
        return pojo;
    }

    //固定标签
    @RequestMapping(value = "/fastenSign", produces = "application/json")
    public FlagPojo fastenSign(HttpServletRequest request, @RequestParam Map<String, Object> params) throws IOException {
        Long userId = getUserId(request);
        String tagName = (String) params.get("tagName");
        String param = (String) params.get("param");
        Integer pageType = Integer.parseInt((String) params.get("pageType"));
        FlagPojo pojo = fastenSignImpl.fastenSign(userId, tagName, param, pageType);
        return pojo;
    }

    @RequestMapping(value = "/pullSignById", produces = "application/json")
    public FlagPojo pullSignById(@RequestParam Map<String, Object> params) throws IOException {
        Long signId = Long.parseLong((String) params.get("signId"));
        AdFastenSign fastenSign = fastenSignImpl.qryById(signId);
        FlagPojo pojo = new FlagPojo();
        if (fastenSign != null) {
            fastenSignImpl.delete(fastenSign);
        } else {
            pojo.setFlag("false");
            pojo.setRetMessage("找不到对应数据");
        }
        return pojo;
    }

    @RequestMapping(value = "/qryFastenSign", produces = "application/json")
    public String qryFastenSign(HttpServletRequest request) {
        AdFastenSignPojo pojo = new AdFastenSignPojo();
        List<AdFastenSignPojoExt> extList = new ArrayList<>();
        Long userId = (Long) request.getSession().getAttribute("userId");
        List<AdFastenSign> fastenSignList = fastenSignImpl.qryByUser(userId);
        for (AdFastenSign sign : fastenSignList) {
            AdFastenSignPojoExt ext = new AdFastenSignPojoExt();
            ext.setSignId(sign.getSignId() + "");
            ext.setPageType(sign.getSignType());
            ext.setParam(sign.getSignParam());
            ext.setTagName(sign.getSignName());
            extList.add(ext);
        }
        pojo.setData(extList);
        return JsonpUtil.modelToJson(pojo);
    }

}
