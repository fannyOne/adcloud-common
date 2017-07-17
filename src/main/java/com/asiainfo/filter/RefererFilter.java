package com.asiainfo.filter;

import com.asiainfo.util.CommConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by weif on 2017/5/2.
 */
@Component
public class RefererFilter implements Filter {

    @Value("${filter.CheckIp:}")
    String CheckIp;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        String referer = ((HttpServletRequest) request).getHeader("Referer");
        if ((referer != null && referer.contains(request.getServerName())) || notNeedCheck(request.getServerName(),((HttpServletRequest) request).getRequestURI())) {
            chain.doFilter(request, response);
        } else {
            return;
        }
    }

    @Override
    public void destroy() {

    }

    public boolean notNeedCheck(String serverIp,String requestUrl) {
        if (StringUtils.isNotEmpty(CheckIp)) {
            if ((serverIp != null && CheckIp.indexOf(serverIp) >= 0)||(requestUrl!=null && CommConstants.filterUncheckUrl.UN_CHECK_URL.contains(requestUrl.substring(requestUrl.lastIndexOf("/")+1)))) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }
}
