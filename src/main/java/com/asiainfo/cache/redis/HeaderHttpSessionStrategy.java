package com.asiainfo.cache.redis;

import org.springframework.session.Session;
import org.springframework.session.web.http.HttpSessionStrategy;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HeaderHttpSessionStrategy implements HttpSessionStrategy {
    private String headerName = "x-auth-token";

    public String getRequestedSessionId(HttpServletRequest request) {
        return request.getHeader(this.headerName);
    }

    public void onNewSession(Session session, HttpServletRequest request,
                             HttpServletResponse response) {
        response.setHeader(this.headerName, session.getId());
    }

    public void onInvalidateSession(HttpServletRequest request,
                                    HttpServletResponse response) {
        response.setHeader(this.headerName, "");
    }

    public void setHeaderName(String headerName) {
        Assert.notNull(headerName, "headerName cannot be null");
        this.headerName = headerName;
    }
}
