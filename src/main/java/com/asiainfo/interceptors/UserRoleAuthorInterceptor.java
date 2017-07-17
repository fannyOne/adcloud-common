package com.asiainfo.interceptors;

import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.models.AdUserRoleRel;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import static com.asiainfo.util.CommConstants.Role.UN_CHECK_METHOD;

/**
 * Created by YangRY
 * 2016/7/25 0025.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class UserRoleAuthorInterceptor implements HandlerInterceptor {
    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        try {
            String uri = request.getRequestURI();
            if (UN_CHECK_METHOD.contains(uri)) {
                return true;
            }
            String userName = "";
            HttpSession session = request.getSession();
            userName = (String) session.getAttribute("username");
            if (userName != null) {
                List<AdUserRoleRel> userRoleRelList = userRoleRelImpl.qryByUser(userName);
                if (userRoleRelList != null) {
                    for (AdUserRoleRel userRoleRel : userRoleRelList) {
                        if (userRoleRel.getAdRole().getRoleLevel() <= 0) {
                            return true;
                        }
                    }
                }
            }
            response.sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "无访问权限");
            return false;
        } catch (Exception e) {
            response.sendError(CommConstants.Role.ERR_OUT_OF_ROLE, "无访问权限");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
    }
}
