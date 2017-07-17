package com.asiainfo.interceptors;

import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.build.service.impl.AdUserImpl;
import com.asiainfo.comm.module.models.AdUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static com.asiainfo.util.CommConstants.Role.UN_CHECK_METHOD;

/**
 * Created by Administrator on 2016/7/28.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    AdUserImpl userImpl;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if ("OPTIONS".equals(httpServletRequest.getMethod())) {
            return true;
        }
        String uri = httpServletRequest.getRequestURI();
        if (UN_CHECK_METHOD.contains(uri) || uri.contains("/auth/") || uri.contains("/test/") || uri.contains("/groupUser/") || uri.contains("/api-docs") || uri.contains("/v1/businessSystem")) {
            return true;
        }
        if (uri.contains("/linkGitLab/") && null != httpServletRequest.getSession().getAttribute("PORTAL_POPEDOMIMPL_SESSION")) {
            return true;
        }
        HttpSession session = httpServletRequest.getSession();
        String userName = (String) session.getAttribute("username");
        if (StringUtils.isNotEmpty(userName)) {
            return true;
        } else {
            httpServletResponse.sendError(CommConstants.Role.ERR_NOT_LOGIN, "请先登录！");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        String uri = httpServletRequest.getRequestURI();
        if (UN_CHECK_METHOD.contains(uri) || uri.contains("/auth/") || uri.contains("/test/") || uri.contains("/groupUser/")) {
            return;
        }
        HttpSession session = httpServletRequest.getSession();
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            AdUser user = userImpl.qryById(userId);
            user.setActiveDate(new Date());
            userImpl.updateUser(user);
        }
    }
}
