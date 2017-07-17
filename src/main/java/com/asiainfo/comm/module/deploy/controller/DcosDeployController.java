package com.asiainfo.comm.module.deploy.controller;

import com.asiainfo.comm.common.pojo.pojoMaster.AdEnvPojo;
import com.asiainfo.util.JsonUtil;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.comm.module.deploy.service.impl.DcosDeployInfoImpl;
import com.asiainfo.comm.module.models.AdDcosDeployInfo;
import com.asiainfo.comm.module.models.AdProject;
import com.asiainfo.comm.module.role.service.impl.AdProjectImpl;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.asiainfo.comm.module.role.service.impl.VerifyRightImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weif on 2016/9/29.
 */
@RestController
@RequestMapping("/DcosDeploy")
public class DcosDeployController {

    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;
    @Autowired
    VerifyRightImpl verifyRight;
    @Autowired
    AdProjectImpl projectImpl;
    @Autowired
    private DcosDeployInfoImpl dcosDeployInfoImpl;

    @RequestMapping(value = "/getDcosDeployInfo", produces = "application/json")
    public String getDcosDeployInfo(@RequestParam Map map) throws Exception {
        Long projectId;
        Map<String, Object> hmap = new HashMap<String, Object>();
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
            List<AdDcosDeployInfo> adDcosDeployInfoList = dcosDeployInfoImpl.qryDcosDeployInfoByProjectId(projectId);
            hmap.put("branchList", adDcosDeployInfoList);
        }
        String retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }

    @RequestMapping(value = "/project/qryEnvInfo", produces = "application/json")
    public String qryEnvInfo(@RequestParam Map map) throws Exception {
        AdEnvPojo poj;
        long groupId;
        String branchType = "";
        Long projectId = Long.parseLong((String) map.get("projectId"));
        AdProject project = projectImpl.qryProject(projectId);
        if (project != null) {
            groupId = project.getAdGroup().getGroupId();
        } else {
            throw new Exception("应用不存在！");
        }
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest();
        HttpSession httpSession = httpServletRequest.getSession();
        String username = (String) httpSession.getAttribute("username");
        int deployRole = verifyRight.isRelGroupDeploy(groupId, username);
        if (deployRole == 2) {
            branchType = "1";
        } else if (deployRole == 1) {
            branchType = "2";
        } else if (deployRole >= 3) {
            branchType = "1,2";
        }
        poj = dcosDeployInfoImpl.qryEnvInfo(projectId, branchType);
        return JsonpUtil.modelToJson(poj);
    }
    /********************************改造方法*****************************************/
    /**
     * @param map 前台传递的参数map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/project/qryEnvInfoByRegion", produces = "application/json")
    public String qryEnvInfoByRegion(@RequestParam Map map) throws Exception {
        AdEnvPojo poj;                                                                  //查询到的poj
//        long groupId;                                                                   //groupId
        Integer region = 0;                                                             //所属域
        Long projectId = Long.parseLong((String) map.get("projectId"));                 //获得应用id
        region = Integer.parseInt(map.get("region").toString());                        //获得所属域
        AdProject project = projectImpl.qryProject(projectId);                          //根据id查询得到项目
        if (project == null) {                                                          //应用存在
            throw new Exception("应用不存在！");
        }
        poj = dcosDeployInfoImpl.qryEnvInfo(projectId, "" + region);      //获得poj
        return JsonpUtil.modelToJson(poj);
    }
}
