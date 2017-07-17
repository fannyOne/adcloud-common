package com.asiainfo.comm.module.busiLog.interceptor;

import com.asiainfo.comm.common.enums.BusiCode;
import com.asiainfo.comm.common.pojo.pojoExt.*;
import com.asiainfo.comm.module.build.dao.impl.AdVirtualEnvironmentDAO;
import com.asiainfo.comm.module.build.service.impl.AdBranchImpl;
import com.asiainfo.comm.module.build.service.impl.AdDcosDeployInfoImpl;
import com.asiainfo.comm.module.build.service.impl.AdFastenSignImpl;
import com.asiainfo.comm.module.build.service.impl.AdGroupImpl;
import com.asiainfo.comm.module.busiLog.service.impl.AdBusiLogImpl;
import com.asiainfo.comm.module.deploy.service.impl.DcosDeployInfoImpl;
import com.asiainfo.comm.module.deploy.service.impl.VirtualDeployInfoImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.role.service.impl.AdProjectImpl;
import com.avaje.ebean.TxType;
import com.avaje.ebean.annotation.Transactional;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;

@Component
@Aspect
@lombok.extern.slf4j.Slf4j
public class AdBusiLogInterceptor {
    private static final String authPackage = "execution(* com.asiainfo.auth.";
    private static final String buildPackage = "execution(* com.asiainfo.comm.module.build.controller.";
    private static final String deployPackage = "execution(* com.asiainfo.comm.module.deploy.controller.";
    private static final String rolePackage = "execution(* com.asiainfo.comm.module.role.controller.";
    private static final String OAuth2Controller = "OAuth2Controller";
    private static final String operationController = "AdOperationController";
    private static final String branchController = "AdBranchController";
    private static final String jenkinsController = "JenkinsController";
    private static final String systemDeployController = "SystemDeployController";
    private static final String groupUserController = "GroupUserController";
    private static final String envSetController = "EvnSetController";
    private static final String deployManagerController = "DeployManagerController";
    private static final String overStr = "(..))";
    @Autowired
    AdBusiLogImpl adBusiLogImpl;
    @Autowired
    AdBranchImpl branchImpl;
    @Autowired
    AdGroupImpl groupImpl;
    @Autowired
    AdProjectImpl projectImpl;
    @Autowired
    AdFastenSignImpl fastenSignImpl;
    @Autowired
    AdGroupImpl adGroupImpl;
    @Autowired
    AdVirtualEnvironmentDAO virtualEnvironmentDAO;
    @Autowired
    AdDcosDeployInfoImpl dcosDeployInfoImpl;
    @Autowired
    DcosDeployInfoImpl dcosDeployInfoImpl2;
    @Autowired
    VirtualDeployInfoImpl virtualDeployInfoImpl;

    /*
    * authPackage
    */
    // OAuth2Controller
    @AfterReturning(authPackage + OAuth2Controller + ".login" + overStr)
    public void login() {
        // 语句参数
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        String desc = BusiCode.SIGN_IN.getDescription();
        String type = "系统";
        String obj = "ADCloud";
        // 自定义参数
        String result = checkSuccess(response);
        otherLog(BusiCode.SIGN_IN.getCode(), obj, type, desc, null, result, request);
    }

    @Before(authPackage + OAuth2Controller + ".logout" + overStr)
    public void logout() {
        // 语句参数
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String result = null;
        String desc = BusiCode.SIGN_OUT.getDescription();
        String type = "系统";
        String obj = "ADCloud";
        otherLog(BusiCode.SIGN_OUT.getCode(), obj, type, desc, null, result, request);
    }

    /*
    * authPackage end
    */

    /*
    * buildPackage
    */
    // AdOperationController
    @AfterReturning(buildPackage + operationController + ".envRun" + overStr)
    public void envRun() {
        branchLog(BusiCode.BUILD_START.getCode(), BusiCode.BUILD_START.getDescription(), null, null, null);
    }

    @AfterReturning(buildPackage + operationController + ".pullSignById" + overStr)
    public void pullSignById() {
        // 语句参数
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String result = null;
        String desc = BusiCode.FASTEN_SIGN_PULL.getDescription();
        String type = "页签";
        String signName = "";
        // 自定义参数
        String signIdStr = request.getParameter("signId");
        if (signIdStr == null || signIdStr.isEmpty()) {
            result = "失败：参数错误";
        } else {
            Long signId = Long.parseLong(signIdStr);
            AdFastenSign fastenSign = fastenSignImpl.qryByIdNoState(signId);
            if (fastenSign == null) {
                result = "失败：对象不存在";
            } else {
                signName = fastenSign.getSignName() + "(" + signIdStr + ")";
            }
        }
        otherLog(BusiCode.FASTEN_SIGN_PULL.getCode(), signName, type, desc, null, result, request);
    }

    @AfterReturning(buildPackage + operationController + ".fastenSign" + overStr)
    public void fastenSign() {
        // 语句参数
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String type = "页签";
        String signName = "";
        // 自定义参数
        signName = request.getParameter("tagName");
        otherLog(BusiCode.FASTEN_SIGN_IN.getCode(), signName, type, BusiCode.FASTEN_SIGN_IN.getDescription(), null, null, request);
    }

    // AdBranchController
    @AfterReturning(buildPackage + branchController + ".rollBack" + overStr)
    public void rollBack() {
        branchLog(BusiCode.ROLL_BACK.getCode(), BusiCode.ROLL_BACK.getDescription(), null, null, null);
    }

    @AfterReturning(buildPackage + branchController + ".restart" + overStr)
    public void restart() {
        branchLog(BusiCode.RESTART.getCode(), BusiCode.RESTART.getDescription(), null, null, null);
    }

    @AfterReturning(buildPackage + branchController + ".redeploy" + overStr)
    public void redeploy() {
        branchLog(BusiCode.RE_DEPLOY.getCode(), BusiCode.RE_DEPLOY.getDescription(), null, null, null);
    }

    //JenkinsController
    @AfterReturning(buildPackage + jenkinsController + ".ManualHand" + overStr)
    public void ManualHand() {
        branchLog(BusiCode.BUILD_START.getCode(), BusiCode.BUILD_START.getDescription(), "envId", null, null);
    }

    @AfterReturning(buildPackage + jenkinsController + ".StopPipeline" + overStr)
    public void StopPipeline() {
        branchLog(BusiCode.BUILD_STOP.getCode(), BusiCode.BUILD_STOP.getDescription(), "envId", null, null);
    }

    @AfterReturning(buildPackage + jenkinsController + ".UpJkConfig" + overStr)
    public void UpJkConfig(JoinPoint joinPoint) {
        ReqStagePojo poj = (ReqStagePojo) joinPoint.getArgs()[0];
        String branchIdStr = poj.getBranchid();
        Long branchId = null;
        if (branchIdStr != null && StringUtils.isNotEmpty(branchIdStr)) {
            branchId = Long.parseLong(branchIdStr);
        }
        branchLog(BusiCode.UPDATE_BRANCH_JK.getCode(), BusiCode.UPDATE_BRANCH_JK.getDescription(), null, branchId, null);
    }
    /*
    * buildPackage end
    */

    /*
    * deployPackage
    */

    @AfterReturning(deployPackage + systemDeployController + ".AddGroup" + overStr)
    public void AddGroup() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String obj = request.getParameter("groupName");
        // 自定义参数
        groupLog(BusiCode.ADD_GROUP.getCode(), BusiCode.ADD_GROUP.getDescription(), null, null, obj);
    }

    @AfterReturning(deployPackage + systemDeployController + ".AddProject" + overStr)
    public void AddProject() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        String type = "应用";
        String obj = request.getParameter("projectName");
        // 自定义参数
        String result = checkSuccess(response);
        otherLog(BusiCode.ADD_PROJECT.getCode(), obj, type, BusiCode.ADD_PROJECT.getDescription(), null, result, request);
    }

    @Before(deployPackage + systemDeployController + ".delGroup" + overStr)
    public void delGroup() {
        groupLog(BusiCode.DEL_GROUP.getCode(), BusiCode.DEL_GROUP.getDescription(), null, null, null);
    }

    @Before(deployPackage + systemDeployController + ".delProject" + overStr)
    public void delProject() {
        projectLog(BusiCode.DEL_PROJECT.getCode(), BusiCode.DEL_PROJECT.getDescription(), null, null, null);
    }

    @AfterReturning(deployPackage + systemDeployController + ".addSystemDeploy" + overStr)
    public void addSystemDeploy(JoinPoint joinPoint) throws IOException {
        // 语句参数
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String desc = BusiCode.ADD_BRANCH.getDescription();
        String type = "流水";
        String branchName = null;
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        // 自定义参数
        String result = checkSuccess(response);
        SysProjectPojoExt poj = (SysProjectPojoExt) joinPoint.getArgs()[0];
        String projectIdStr = poj.getProjectId();
        Long projectId = null;
        if (projectIdStr != null && StringUtils.isNotEmpty(projectIdStr)) {
            projectId = Long.parseLong(projectIdStr);
        }
        if (poj.getObj() != null && poj.getObj().length > 0) {
            StringBuilder sb = new StringBuilder("");
            for (SysBranchPojoExt ext : poj.getObj()) {
                sb.append(ext.getBranchName()).append(",");
            }
            branchName = sb.toString();
            branchName = branchName.substring(0, branchName.length() - 1);
        }
        otherLog(BusiCode.ADD_BRANCH.getCode(), branchName, type, desc, projectId, result, request);
    }

    @Before(deployPackage + systemDeployController + ".delBranch" + overStr)
    public void delBranch() {
        branchLog(BusiCode.DEL_BRANCH.getCode(), BusiCode.DEL_BRANCH.getDescription(), null, null, null);
    }

    @AfterReturning(deployPackage + systemDeployController + ".updateSystemDeploy" + overStr)
    public void updateSystemDeploy(JoinPoint joinPoint) throws IOException {
        // 语句参数
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String desc = BusiCode.UPDATE_BRANCH.getDescription();
        String type = "流水";
        String branchName = null;
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        // 自定义参数
        String result = checkSuccess(response);
        Long projectId = null;
        SysReformProjectPojoExt poj = (SysReformProjectPojoExt) joinPoint.getArgs()[0];
        if (poj.getObj() != null && poj.getObj().length > 0) {
            AdBranch branch = branchImpl.qryById(Long.parseLong(poj.getObj()[0].getBranchId()));
            if (branch != null && branch.getAdProject() != null) {
                projectId = branch.getAdProject().getProjectId();
            }
            StringBuilder sb = new StringBuilder("");
            for (SysReformBranchPojoExt ext : poj.getObj()) {
                sb.append(ext.getBranchName()).append("(").append(ext.getBranchId()).append(")").append(",");
            }
            branchName = sb.toString();
            branchName = branchName.substring(0, branchName.length() - 1);
        }
        otherLog(BusiCode.UPDATE_BRANCH.getCode(), branchName, type, desc, projectId, result, request);
    }

    @AfterReturning(deployPackage + systemDeployController + ".copySystemDeploy" + overStr)
    public void copySystemDeploy(JoinPoint joinPoint) throws IOException {
        // 语句参数
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        // 自定义参数
        String result = checkSuccess(response);
        BranchCopyPojoExt poj = (BranchCopyPojoExt) joinPoint.getArgs()[0];

        HttpSession session = request.getSession();
        StringBuilder sb = new StringBuilder();
        sb.append(session.getAttribute("displayName")).append("(").append(session.getAttribute("username")).append(")")
            .append("将").append("【").append(poj.getSrcBranchId()).append("】流水复制给")
            .append("【").append(poj.getBranchDesc()).append("(").append(poj.getBranchName()).append(")】");
        if (result != null) {
            sb.append(result);
        }
        AdBusiLog busiLog = new AdBusiLog();
        busiLog.setOpId((Long) session.getAttribute("userId"));
        busiLog.setOpName((String) session.getAttribute("username"));
        busiLog.setBusiCode(BusiCode.COPY_BRANCH.getCode());
        busiLog.setBusiDetail(sb.toString());
        busiLog.setProjectId(null);
        busiLog.setCreateDate(new Date());
        adBusiLogImpl.save(busiLog);
    }

    @AfterReturning(deployPackage + envSetController + ".addVmEnv" + overStr)
    public void addVmEnv() throws IOException {
        branchLog(BusiCode.ENV_VM_ADD.getCode(), BusiCode.ENV_VM_ADD.getDescription(), "virtualName", null, null);
    }


    @AfterReturning(deployPackage + envSetController + ".updateVmEnv" + overStr)
    public void updateVmEnv(JoinPoint joinPoint) throws IOException {
        vmEvnPojoExt ext = (vmEvnPojoExt) joinPoint.getArgs()[0];
        if (ext == null) {
            return;
        }
        Long vmId = ext.getVirtualId();
        if (vmId != 0) {
            AdVirtualEnvironment vm = virtualEnvironmentDAO.qryById(vmId);
            if (vm != null) {
                long branchId = vm.getAdBranch().getBranchId();
                branchLog(BusiCode.ENV_VM_MODIFY.getCode(), BusiCode.ENV_VM_MODIFY.getDescription(), null, branchId, null);
            }
        }
    }

    @AfterReturning(deployPackage + envSetController + ".deleteVmSingleInfo" + overStr)
    public void deleteVmSingleInfo() throws IOException {
        branchLog(BusiCode.ENV_VM_DEL.getCode(), BusiCode.ENV_VM_DEL.getDescription(), "BranchId", null, null);
    }

    @AfterReturning(deployPackage + envSetController + ".addDcosEnvInfo" + overStr)
    public void addDcosEnvInfo() throws IOException {
        branchLog(BusiCode.ENV_DCOS_ADD.getCode(), BusiCode.ENV_DCOS_ADD.getDescription(), "dcosBranchId", null, null);
    }

    @AfterReturning(deployPackage + envSetController + ".updateDcosEnv" + overStr)
    public void updateDcosEnv(JoinPoint joinPoint) throws IOException {
        dcosEnvPojoExt ext = (dcosEnvPojoExt) joinPoint.getArgs()[0];
        if (ext != null) {
//            Long dcosId = ext.getInfoId();
//            AdDcosDeployInfo dcosDeployInfo = dcosDeployInfoImpl.qryById(dcosId);
//            if (dcosDeployInfo != null) {
//                long branchId = dcosDeployInfo.getAdBranch().getBranchId();
            projectLog(BusiCode.ENV_DCOS_MODIFY.getCode(), BusiCode.ENV_DCOS_MODIFY.getDescription(), null, ext.getProjectId(), null);
//            }
        }
    }

    @AfterReturning(deployPackage + envSetController + ".deleteDcosSingleInfo" + overStr)
    public void deleteDcosSingleInfo() throws IOException {
        branchLog(BusiCode.ENV_DCOS_DEL.getCode(), BusiCode.ENV_DCOS_DEL.getDescription(), "dcosBranchId", null, null);
    }

    @AfterReturning(deployPackage + deployManagerController + ".deployPlan" + overStr)
    public void deployPlan() throws IOException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String env = request.getParameter("env");
        long projectId;
        long envId;
        String envType;
        if (env != null && StringUtils.isNotEmpty(env)) {
            String[] envStrList = env.split("_");
            envId = Long.parseLong(envStrList[0]);
            envType = envStrList[1];
            projectId = checkEnvType(envId, envType);
            projectLog(BusiCode.RELEASE_ADD.getCode(), BusiCode.RELEASE_ADD.getDescription(), null, projectId, null);
        }
    }

    @AfterReturning(deployPackage + deployManagerController + ".modifyDeployPlan" + overStr)
    public void modifyDeployPlan() throws IOException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String env = request.getParameter("env");
        long projectId;
        long envId;
        String envType;
        if (env != null && StringUtils.isNotEmpty(env)) {
            String[] envStrList = env.split("_");
            envId = Long.parseLong(envStrList[0]);
            envType = envStrList[1];
            projectId = checkEnvType(envId, envType);
            projectLog(BusiCode.RELEASE_MODIFY.getCode(), BusiCode.RELEASE_MODIFY.getDescription(), null, projectId, null);
        }
    }

    public long checkEnvType(long envId, String envType) {
        long projectId;
        if (envType.equals("dcos")) {
            AdDcosDeployInfo dcosDeployInfo = dcosDeployInfoImpl2.qryDcosDeployInfoById(envId);
            projectId = dcosDeployInfo.getAdProject().getProjectId();
        } else {
            AdVirtualEnvironment virtualEnvironment = virtualDeployInfoImpl.qryById(envId);
            projectId = virtualEnvironment.getAdProject().getProjectId();
        }
        return projectId;
    }

    @AfterReturning(deployPackage + deployManagerController + ".closeDeployPlan" + overStr)
    public void closeDeployPlan() throws IOException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String env = request.getParameter("1292");
        long projectId;
        long envId;
        String envType;
        if (env != null && StringUtils.isNotEmpty(env)) {
            String[] envStrList = env.split("_");
            envId = Long.parseLong(envStrList[0]);
            envType = envStrList[1];
            projectId = checkEnvType(envId, envType);
            projectLog(BusiCode.RELEASE_ADD.getCode(), BusiCode.RELEASE_ADD.getDescription(), null, projectId, null);
        }
    }

    /*
    * thirdPackage end
    */

    /*
    * rolePackage
    */

    // GroupUserController
    @AfterReturning(rolePackage + groupUserController + ".updateUsers" + overStr)
    public void updateUsers(JoinPoint joinPoint) throws IOException {
        // 语句参数
        Long groupId;
        GroupUserExtPojo poj = (GroupUserExtPojo) joinPoint.getArgs()[0];
        groupId = poj.getGroupId();
        groupLog(BusiCode.UPDATE_GROUP_MEMBER.getCode(), BusiCode.UPDATE_GROUP_MEMBER.getDescription(), null, groupId, null);
    }

    @AfterReturning(rolePackage + groupUserController + ".delUser" + overStr)
    public void delUser(JoinPoint joinPoint) throws IOException {
        // 语句参数
        Long groupId;
        GroupUserDelExtPojo poj = (GroupUserDelExtPojo) joinPoint.getArgs()[0];
        groupId = poj.getGroupId();
        groupLog(BusiCode.DEL_GROUP_MEMBER.getCode(), BusiCode.DEL_GROUP_MEMBER.getDescription() + "(" + poj.getUserName() + ")", null, groupId, null);
    }

    private String checkSuccess(HttpServletResponse response) {
        String result = null;
        int status = response.getStatus();
        // 自定义参数
        if (status != 200) {
            result = "失败";
        }
        return result;
    }

    /*
    * rolePackage end
    */

    private void branchLog(long busiCode, String desc, String branchIdStr, Long branchId, String branchName) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        long projectId = -1;
        if (branchName == null && branchId == null) {
            branchId = -1L;
            String branchIdKey = "branchId";
            if (branchIdStr != null) {
                branchIdKey = branchIdStr;
            }
            String branchIdObj = request.getParameter(branchIdKey);
            if (branchIdObj == null || branchIdObj.isEmpty()) {
                branchName = "未知";
            } else {
                branchId = Long.parseLong(branchIdObj);
            }
        }
        if (branchId != null && branchId != -1L) {
            AdBranch branch = branchImpl.qryById(branchId);
            if (branch == null) {
                branchName = "未知";
            } else {
                if (branchName == null) {
                    branchName = branch.getBranchName();
                }
                projectId = branch.getAdProject().getProjectId();
            }
        }
        try {
            String sb = String.valueOf(session.getAttribute("displayName")) +
                "(" + session.getAttribute("username") +
                ")" + "对" + "【" + branchName + "(" +
                branchId + ")" + "】" + "流水进行" +
                "【" + desc + "】" + "操作";
            AdBusiLog busiLog = new AdBusiLog();
            busiLog.setOpId((Long) session.getAttribute("userId"));
            busiLog.setOpName((String) session.getAttribute("username"));
            busiLog.setBusiCode(busiCode);
            busiLog.setBusiDetail(sb);
            busiLog.setProjectId(projectId);
            busiLog.setCreateDate(new Date());
            adBusiLogImpl.save(busiLog);
        } catch (Exception e) {
            FailedLog(session, e);
        }
    }

    private void groupLog(long busiCode, String desc, String groupIdStr, Long groupId, String groupName) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        if (groupName == null && groupId == null) {
            groupId = -1L;
            String groupIdKey = "groupId";
            if (groupIdStr != null) {
                groupIdKey = groupIdStr;
            }
            String groupIdObj = request.getParameter(groupIdKey);
            if (groupIdObj == null || groupIdObj.isEmpty()) {
                groupName = "未知";
            } else {
                groupId = Long.parseLong(groupIdObj);

            }
        }
        if (groupId != null && groupId != -1L) {
            AdGroup group = groupImpl.qryById(groupId);
            if (group == null) {
                groupName = "未知";
            } else {
                if (groupName == null) {
                    groupName = group.getGroupName();
                }
            }
        }
        try {
            String sb = String.valueOf(session.getAttribute("displayName")) +
                "(" + session.getAttribute("username") +
                ")" + "对" + "【" + groupName + "(" +
                groupId + ")" + "】" + "项目进行" +
                "【" + desc + "】" + "操作";
            AdBusiLog busiLog = new AdBusiLog();
            busiLog.setOpId((Long) session.getAttribute("userId"));
            busiLog.setOpName((String) session.getAttribute("username"));
            busiLog.setBusiCode(busiCode);
            busiLog.setBusiDetail(sb);
            busiLog.setProjectId(null);
            busiLog.setCreateDate(new Date());
            adBusiLogImpl.save(busiLog);
        } catch (Exception e) {
            FailedLog(session, e);
        }
    }

    private void projectLog(long busiCode, String desc, String projectIdStr, Long projectId, String projectName) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        if (projectId == null && projectName == null) {
            projectId = -1L;
            String projectIdKey = "projectId";
            if (projectIdStr != null) {
                projectIdKey = projectIdStr;
            }
            String projectIdObj = request.getParameter(projectIdKey);
            if (projectIdObj == null || projectIdObj.isEmpty()) {
                projectName = "未知";
            } else {
                projectId = Long.parseLong(projectIdObj);
            }
        }
        if (projectId != null && projectId != -1) {
            AdProject project = projectImpl.qryProject(projectId);
            if (project == null) {
                projectName = "未知";
            } else {
                if (projectName == null) {
                    projectName = project.getProjectName();
                }
            }
        }
        try {
            String sb = String.valueOf(session.getAttribute("displayName")) +
                "(" + session.getAttribute("username") +
                ")" + "对" + "【" + projectName + "(" +
                projectId + ")" + "】" + "应用进行" +
                "【" + desc + "】" + "操作";
            AdBusiLog busiLog = new AdBusiLog();
            busiLog.setOpId((Long) session.getAttribute("userId"));
            busiLog.setOpName((String) session.getAttribute("username"));
            busiLog.setBusiCode(busiCode);
            busiLog.setBusiDetail(sb);
            busiLog.setProjectId(projectId);
            busiLog.setCreateDate(new Date());
            adBusiLogImpl.save(busiLog);
        } catch (Exception e) {
            FailedLog(session, e);
        }
    }

    private void otherLog(long busiCode, String obj, String type, String opt, Long projectId, String result, HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(session.getAttribute("displayName"))
                .append("(").append(session.getAttribute("username"))
                .append(")").append("对").append("【").append(obj)
                .append("】").append(type).append("进行")
                .append("【").append(opt).append("】").append("操作");
            if (result != null) {
                sb.append(result);
            }
            AdBusiLog busiLog = new AdBusiLog();
            busiLog.setOpId((Long) session.getAttribute("userId"));
            busiLog.setOpName((String) session.getAttribute("username"));
            busiLog.setBusiCode(busiCode);
            busiLog.setBusiDetail(sb.toString());
            busiLog.setProjectId(projectId);
            busiLog.setCreateDate(new Date());
            adBusiLogImpl.save(busiLog);
        } catch (Exception e) {
            FailedLog(session, e);
        }
    }

    @Transactional(type = TxType.REQUIRES_NEW)
    private void FailedLog(HttpSession session, Exception e) {
        AdBusiLog busiLog = new AdBusiLog();
        busiLog.setOpId((Long) session.getAttribute("userId"));
        busiLog.setOpName((String) session.getAttribute("username"));
        busiLog.setBusiCode(-2L);
        busiLog.setBusiDetail("ERROR:" + e.getMessage());
        busiLog.setProjectId(-2L);
        busiLog.setCreateDate(new Date());
        adBusiLogImpl.save(busiLog);
    }
}
