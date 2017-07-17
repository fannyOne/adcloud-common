package com.asiainfo.filter;

import com.asiainfo.util.CommConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by weif on 2017/5/3.
 */
@Component
public class CorpsFilter implements Filter {

    @Value("${filter.CorpsUrl:}")
    String CorpsUrl;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String originHeader = request.getHeader("Origin");
        if (checkCorpsUrl(originHeader,request.getRequestURI())) {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    public boolean checkCorpsUrl(String originhead,String requestUrl) {
        if (StringUtils.isNotEmpty(CorpsUrl)) {
            if ((originhead != null && CorpsUrl.indexOf(originhead) >= 0)||(requestUrl!=null && CommConstants.filterUncheckUrl.UN_CHECK_URL.contains(requestUrl.substring(requestUrl.lastIndexOf("/")+1)))) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

}
