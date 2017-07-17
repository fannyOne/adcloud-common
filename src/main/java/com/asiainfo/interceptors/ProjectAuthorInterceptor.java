package com.asiainfo.interceptors;

import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.build.dao.impl.*;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdPipeLineState;
import com.asiainfo.comm.module.models.AdStage;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.asiainfo.util.CommConstants.Role.UN_CHECK_METHOD;

/**
 * Created by Administrator on 2016/8/4.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class ProjectAuthorInterceptor implements HandlerInterceptor {
    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;
    @Autowired
    AdBranchDAO branchDAO;
    @Autowired
    AdStageDAO stageDAO;
    @Autowired
    AdPipeLineStateDAO pipeLineStateDAO;
    @Autowired
    AdProjectDAO projectDAO;
    @Autowired
    AdGroupDAO groupDAO;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String uri = httpServletRequest.getRequestURI();
        if (UN_CHECK_METHOD.contains(uri)) {
            return true;
        }
        HttpSession httpSession = httpServletRequest.getSession();
        long value;
        String valueStr;
        long projectId = 0;
        boolean unKnow = false;
        boolean hasParam = true;
        boolean isGroup = false;
        if (uri.contains("/project/")) {
            valueStr = httpServletRequest.getParameter("projectId");
            if (StringUtils.isNotEmpty(valueStr)) {
                value = Long.parseLong(valueStr);
                if (projectDAO.qryRowById(value) <= 0) {
                    unKnow = true;
                } else {
                    projectId = value;
                }
            } else {
                hasParam = false;
            }
        } else if (uri.contains("/stage/")) {
            valueStr = httpServletRequest.getParameter("stageId");
            if (StringUtils.isNotEmpty(valueStr)) {
                value = Long.parseLong(valueStr);
                AdStage stage = stageDAO.qryById(value);
                if (stage != null) {
                    projectId = stage.getAdBranch().getAdProject().getProjectId();
                } else {
                    unKnow = true;
                }
            } else {
                hasParam = false;
            }
        } else if (uri.contains("/group/")) {
            valueStr = httpServletRequest.getParameter("groupId");
            if (StringUtils.isNotEmpty(valueStr)) {
                value = Long.parseLong(valueStr);
                if (groupDAO.qryRowById(value) <= 0) {
                    isGroup = true;
                } else {
                    unKnow = true;
                }
            } else {
                hasParam = false;
            }
        } else if (uri.contains("/branch/")) {
            valueStr = httpServletRequest.getParameter("branchId");
            if (StringUtils.isNotEmpty(valueStr)) {
                value = Long.parseLong(valueStr);
                AdBranch branch = branchDAO.qryBranchByid(value);
                if (branch != null) {
                    projectId = branch.getAdProject().getProjectId();
                } else {
                    unKnow = true;
                }
            } else {
                hasParam = false;
            }
        } else if (uri.contains("/pipeline/")) {
            valueStr = httpServletRequest.getParameter("pipeLineId");
            if (StringUtils.isNotEmpty(valueStr)) {
                value = Long.parseLong(valueStr);
                AdPipeLineState state = pipeLineStateDAO.qryById(value);
                if (state != null) {
                    projectId = state.getAdProject().getProjectId();
                } else {
                    unKnow = true;
                }
            } else {
                hasParam = false;
            }
        } else {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "操作验证失败：未知操作。");
            return false;
        }
        if (isGroup && userRoleRelImpl.notAllowGroup((String) httpSession.getAttribute("username"), projectId)) {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "无操作权限！");
            return false;
        }
        if (!hasParam) {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_INNER, "参数传递错误。");
            return false;
        }
        if (unKnow) {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "操作验证失败：没有操作对象。");
            return false;
        }
        if (userRoleRelImpl.notAllow((String) httpSession.getAttribute("username"), projectId)) {
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse().sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "无操作权限！");
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
