package com.asiainfo.comm.module.role.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.GitUserPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.UsersPojo;
import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdPipeLineStateDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDAO;
import com.asiainfo.comm.module.build.dao.impl.AdStageDAO;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.comm.module.build.service.impl.GitlibUserService;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdPipeLineState;
import com.asiainfo.comm.module.models.AdStage;
import com.asiainfo.comm.module.models.AdUserRoleRel;
import com.asiainfo.comm.module.models.functionModels.GitLabUser;
import com.asiainfo.comm.module.role.dao.impl.AdUserRoleRelDAO;
import com.avaje.ebean.PagedList;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YangRY
 * 2016/7/12 0012.
 */
@Component
public class AdUserRoleRelImpl {
    @Autowired
    AdUserRoleRelDAO userRoleRelDAO;

    @Autowired
    GitlibUserService gitlibUserService;
    @Autowired
    AdBranchDAO branchDAO;
    @Autowired
    AdStageDAO stageDAO;
    @Autowired
    AdPipeLineStateDAO pipeLineStateDAO;
    @Autowired
    AdProjectDAO projectDAO;

    @Autowired
    AdStaticDataImpl bsStaticDataImpl;
    @Autowired
    VerifyRightImpl verifyRightImpl;

    public List<AdUserRoleRel> qryByUser(String opName) {
        return userRoleRelDAO.qryByUser(opName);
    }

    public List<AdUserRoleRel> qryAll() {
        return userRoleRelDAO.qryAll();
    }

    public CommonPojo editUserRole(Map<String, String> map) {
        String roleId = map.get("roleId");
        String usersName = map.get("usersName");
        return userRoleRelDAO.qryByUsers(roleId, usersName);
    }

    public UsersPojo qryUserByRole(long roleId, String userName, String pageStr, UsersPojo users, int per_page) {
        int page = 1;
        if (StringUtils.isNotEmpty(pageStr)) {
            page = Integer.parseInt(pageStr);
        }
        PagedList<AdUserRoleRel> relPage = userRoleRelDAO.qryRelByCond(roleId, userName, page - 1, per_page);
        users.setTotal(relPage.getTotalRowCount());
        List<GitUserPojoExt> userPojList = new ArrayList<>();
        List<AdUserRoleRel> relList = relPage.getList();
        for (AdUserRoleRel rel : relList) {
            GitUserPojoExt userPojoExt = new GitUserPojoExt();
            userPojoExt.setRoleId(rel.getAdRole().getRoleId());
            userPojoExt.setRoleName(rel.getAdRole().getRoleName());
            userPojoExt.setRoleLevel(rel.getAdRole().getRoleLevel());
            if (CommConstants.Role.GIT_USERS.containsKey(rel.getUserName())) {
                GitLabUser user = CommConstants.Role.GIT_USERS.get(rel.getUserName());
                userPojoExt.setDisplayName(user.getDisplayName());
                userPojoExt.setEmail(user.getEmail());
                userPojoExt.setId(user.getId());
                userPojoExt.setUsername(user.getUsername());
                userPojList.add(userPojoExt);
            } else {
                Map<String, String> map = new HashMap<>();
                map.put("username", rel.getUserName());
                List<GitUserPojoExt> lists = gitlibUserService.qryUsers(map, 1);
                if (lists != null && lists.size() > 0) {
                    userPojList.add(lists.get(0));
                }
            }
        }
        users.setUsers(userPojList);
        return users;
    }

    public boolean
    verifyPurview(String paramName, long value) throws IOException {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest();
        HttpSession httpSession = httpServletRequest.getSession();
        long projectId = 0;
        boolean allow = true;
        boolean unKnow = false;
        switch (paramName) {
            case "projectId":
                if (projectDAO.qryRowById(value) <= 0) {
                    unKnow = true;
                } else {
                    projectId = value;
                }
                break;
            case "stageId":
                AdStage stage = stageDAO.qryById(value);
                if (stage != null) {
                    projectId = stage.getAdBranch().getAdProject().getProjectId();
                } else {
                    unKnow = true;
                }
                break;
            case "pipelineId":
                AdPipeLineState state = pipeLineStateDAO.qryById(value);
                if (state != null) {
                    projectId = state.getAdProject().getProjectId();
                } else {
                    unKnow = true;
                }
                break;
            case "branchId":
                AdBranch branch = branchDAO.qryBranchByid(value);
                if (branch != null) {
                    projectId = branch.getAdProject().getProjectId();
                } else {
                    unKnow = true;
                }
                break;
            default:
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "操作验证失败：未知操作。");
                return false;
        }
        if (unKnow) {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "操作验证失败：没有操作对象。");
            return false;
        }
        if (notAllow((String) httpSession.getAttribute("username"), projectId)) {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "无操作权限！");
            return false;
        }
        return allow;
    }

    public boolean
    verifyPurviewProjectIds(List<String> projectIds) throws IOException {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest();
        HttpSession httpSession = httpServletRequest.getSession();
        String[] sProjectIds = new String[projectIds.size()];
        projectIds.toArray(sProjectIds);

        if (projectDAO.qryRowByIds(projectIds) < projectIds.size()) {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "操作验证失败：没有操作对象。");
            return false;
        }

        for (String projectId : projectIds) {
            if (notAllow((String) httpSession.getAttribute("username"), Long.parseLong(projectId))) {
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "无操作权限！");
                return false;
            }
        }
        return true;
    }


    public boolean verifyPurview(String paramName, String[] value) throws IOException {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest();
        HttpSession httpSession = httpServletRequest.getSession();
        if (value == null || value.length <= 0) {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "操作验证失败：没有操作对象。");
            return false;
        }
        boolean allow = true;
        if (paramName.equals("projectId")) {
//            projectId = ((AdProject) value).getProjectId();
        }/* else if (paramName.equals("stageId")) {
            projectId = ((AdStage) value).getAdBranch().getAdProject().getProjectId();
        } else if (paramName.equals("pipelineId")) {
            projectId = ((AdPipeLineState) value).getAdProject().getProjectId();
        } else if (paramName.equals("branchId")) {
            projectId = ((AdBranch) value).getAdProject().getProjectId();
        }*/ else {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "操作验证失败：未知操作。");
            return false;
        }
        if (notAllow((String) httpSession.getAttribute("username"), value)) {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "无操作权限！");
            return false;
        }
        return allow;
    }

    public boolean notAllow(String userName, long projectId) {
        return verifyRightImpl.verifyRight(projectId, userName) ? false : true;
    }

    public boolean notAllowGroup(String userName, long groupId) {
        return verifyRightImpl.verifyRightGroup(groupId, userName) ? false : true;
    }

    public boolean notAllow(String userName, String[] projectIds) {
        return verifyRightImpl.verifyRight(projectIds, userName) ? false : true;
    }

    public boolean isGroupAdmin(String userName) {
        if (userName != null) {
            List<AdUserRoleRel> userRoleRelList = qryByUser(userName);
            if (CollectionUtils.isNotEmpty(userRoleRelList)) {
                for (AdUserRoleRel userRoleRel : userRoleRelList) {
                    if (userRoleRel.getAdRole().getRoleLevel() == 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
