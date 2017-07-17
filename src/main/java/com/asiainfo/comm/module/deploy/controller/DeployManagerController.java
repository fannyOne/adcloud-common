package com.asiainfo.comm.module.deploy.controller;

import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDeployPackageDAO;
import com.asiainfo.comm.module.build.dao.impl.AdStageDAO;
import com.asiainfo.comm.module.build.service.impl.AdBranchImpl;
import com.asiainfo.comm.module.build.service.impl.AdProjectGitTagsImpl;
import com.asiainfo.comm.module.build.service.impl.AdStageImpl;
import com.asiainfo.comm.module.build.service.impl.AdUserImpl;
import com.asiainfo.comm.module.deploy.service.impl.DcosDeployInfoImpl;
import com.asiainfo.comm.module.deploy.service.impl.DeployManagerImpl;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployLogImpl;
import com.asiainfo.comm.module.deploy.service.impl.VirtualDeployInfoImpl;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import com.asiainfo.comm.module.models.AdVirtualEnvironment;
import com.asiainfo.comm.module.deploy.service.impl.DcosApiImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guojian on 8/3/16.
 */
@RestController
@RequestMapping("/deployManager")
@lombok.extern.slf4j.Slf4j
public class DeployManagerController {

    @Autowired
    DcosDeployInfoImpl dcosDeployInfoImpl;
    @Autowired
    AdUserImpl adUserImpl;
    @Autowired
    AdProjectGitTagsImpl projectGitTagsImpl;
    @Autowired
    AdProjectDeployPackageDAO adProjectDeployPackageDAO;
    @Autowired
    DeployManagerImpl deployManagerImpl;
    @Autowired
    private DcosApiImpl dcosApiUtil;
    @Autowired
    private AdBranchImpl adBranchImpl;
    @Autowired
    private AdStageImpl adStageImpl;
    @Autowired
    private SystemDeployLogImpl systemDeployLogImpl;
    @Autowired
    private AdStageDAO adStageDAO;
    @Autowired
    private VirtualDeployInfoImpl virtualDeployInfoImpl;

    @RequestMapping(value = "/findByProjectId", method = RequestMethod.POST)
    //@CrossOrigin(origins ="http://localhost:8899")
    public
    @ResponseBody
    List<AdVirtualEnvironment> findByProjectId(@RequestParam("projectId") Long projectId, HttpServletResponse response) {
        //response.setHeader("Access-Control-Allow-Origin", "*");
        //response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        return virtualDeployInfoImpl.findByProjectId(projectId);
    }

    @RequestMapping(value = "/deployVirturl", method = RequestMethod.POST)
    public
    @ResponseBody
    String deployVirturl(HttpServletRequest req, @RequestBody Map<String, Object> params) {
        String commitId = (String) params.get("commitId");
        Long projectId = Long.parseLong((String) params.get("projectId"));
        Long virtualId = Long.parseLong((String) params.get("virtualId"));
        String flag = virtualDeployInfoImpl.deployVirturl(commitId, virtualId);
        return flag;
    }

    //新建发布计划（quartz整改后的整合版本）
    @RequestMapping(value = "/project/deployPlan", method = RequestMethod.POST, produces = "application/json")
    public String deployPlan(@RequestParam Map<String, Object> params, HttpServletRequest request) throws Exception {
        return JsonpUtil.modelToJson(deployManagerImpl.deployPlan(params, request));
    }

    //回滚发布计划（quartz整改后的整合版本）
    @RequestMapping(value = "/project/rollBackDeploy", produces = "application/json")
    public
    @ResponseBody
    String rollBackDeploy(HttpServletRequest request, @RequestParam Map<String, Object> params) throws Exception {
        return JsonpUtil.modelToJson(deployManagerImpl.rollBackDeploy(params, request));
    }

    //删除计划（quartz整改后的版本）
    @RequestMapping(value = "/project/closeDeployPlan", produces = "application/json")
    public String closeDeployPlan(@RequestParam Map params) {
        CommonPojo poj = new CommonPojo();
        Long logId = Long.parseLong((String) params.get("logId"));
        AdSystemDeployLog log = systemDeployLogImpl.qryById(logId);
        log.setDeployResult(5);
        systemDeployLogImpl.addLogsBySystemId(log);
        poj.setRetMessage("success");
        return JsonpUtil.modelToJson(poj);
    }

    //修改发布计划（quartz整改后的整合版本）
    @RequestMapping(value = "/project/modifyDeployPlan", produces = "application/json")
    public String modifyDeployPlan(HttpServletRequest request, @RequestParam Map<String, Object> params) throws Exception {
        return JsonpUtil.modelToJson(deployManagerImpl.modifyDeployPlan(params, request));
    }

    /**
     * 根据id获得已经部署的版本
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/getDeployedVersion", method = RequestMethod.POST)
    public
    @ResponseBody
    List<String> getDeployedVersion(@RequestParam("id") Long id) {
        List<AdSystemDeployLog> logs = systemDeployLogImpl.getLogsBySystemId(id);
        List<String> result = new ArrayList<String>();
        for (AdSystemDeployLog log : logs) {
            result.add(log.getDeployVersion());
        }
        return result;
    }

    @RequestMapping(value = "/stageDeploy", produces = "application/json")
    public
    @ResponseBody
    String stageDeploy(HttpServletRequest req, @RequestBody Map<String, Object> params) {
        boolean flag = false;
        String branchId = (String) params.get("branchId");
        String ip = req.getRemoteAddr();
        String dealResult = (String) params.get("dealResult");
        int li_dealResult = 0;
        try {
            if (StringUtils.isNotEmpty(branchId)) {
                if (StringUtils.isNotEmpty(dealResult) && ("success").equals(dealResult)) {
                    li_dealResult = 1;
                } else {
                    li_dealResult = 2;
                }
                AdBranch adBranch = adBranchImpl.qryById(Long.valueOf(branchId));
                if (adBranch != null) {
                    flag = dcosApiUtil.deployTodocs(adBranch, ip, li_dealResult);
                } else {
                    flag = false;
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        String ret = String.valueOf(flag);
        return ret;
    }
}
