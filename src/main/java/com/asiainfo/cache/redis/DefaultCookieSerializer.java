package com.asiainfo.cache.redis;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultCookieSerializer implements org.springframework.session.web.http.CookieSerializer {
    private String cookieName = "SESSION";

    private Boolean useSecureCookie;

    private boolean useHttpOnlyCookie = isServlet3();

    private String cookiePath;

    private int cookieMaxAge = -1;

    private String domainName;

    private Pattern domainNamePattern;

    private String jvmRoute;

    public List<String> readCookieValues(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        List<String> matchingCookieValues = new ArrayList<String>();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (this.cookieName.equals(cookie.getName())) {
                    String sessionId = cookie.getValue();
                    if (sessionId == null) {
                        continue;
                    }
                    if (this.jvmRoute != null && sessionId.endsWith(this.jvmRoute)) {
                        sessionId = sessionId.substring(0,
                            sessionId.length() - this.jvmRoute.length());
                    }
                    matchingCookieValues.add(sessionId);
                }
            }
        }
        return matchingCookieValues;
    }

    public void writeCookieValue(CookieValue cookieValue) {
        HttpServletRequest request = cookieValue.getRequest();
        HttpServletResponse response = cookieValue.getResponse();

        String requestedCookieValue = cookieValue.getCookieValue();
        String actualCookieValue = this.jvmRoute == null ? requestedCookieValue
            : requestedCookieValue + this.jvmRoute;

        Cookie sessionCookie = new Cookie(this.cookieName, actualCookieValue);
        sessionCookie.setSecure(isSecureCookie(request));
        sessionCookie.setPath(getCookiePath(request));
        String domainName = getDomainName(request);
        if (domainName != null) {
            sessionCookie.setDomain(domainName);
        }

        if (this.useHttpOnlyCookie) {
            sessionCookie.setHttpOnly(true);
        }

        if ("".equals(requestedCookieValue)) {
            sessionCookie.setMaxAge(0);
        } else {
            sessionCookie.setMaxAge(this.cookieMaxAge);
        }

        response.addCookie(sessionCookie);
    }

    public void setUseSecureCookie(boolean useSecureCookie) {
        this.useSecureCookie = useSecureCookie;
    }

    public void setUseHttpOnlyCookie(boolean useHttpOnlyCookie) {
        if (useHttpOnlyCookie && !isServlet3()) {
            throw new IllegalArgumentException(
                "You cannot set useHttpOnlyCookie to true in pre Servlet 3 environment");
        }
        this.useHttpOnlyCookie = useHttpOnlyCookie;
    }

    private boolean isSecureCookie(HttpServletRequest request) {
        if (this.useSecureCookie == null) {
            return request.isSecure();
        }
        return this.useSecureCookie;
    }

    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    public void setCookieName(String cookieName) {
        if (cookieName == null) {
            throw new IllegalArgumentException("cookieName cannot be null");
        }
        this.cookieName = cookieName;
    }

    public void setCookieMaxAge(int cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }

    public void setDomainName(String domainName) {
        if (this.domainNamePattern != null) {
            throw new IllegalStateException(
                "Cannot set both domainName and domainNamePattern");
        }
        this.domainName = domainName;
    }

    public void setDomainNamePattern(String domainNamePattern) {
        if (this.domainName != null) {
            throw new IllegalStateException(
                "Cannot set both domainName and domainNamePattern");
        }
        this.domainNamePattern = Pattern.compile(domainNamePattern,
            Pattern.CASE_INSENSITIVE);
    }

    public void setJvmRoute(String jvmRoute) {
        this.jvmRoute = "." + jvmRoute;
    }

    private String getDomainName(HttpServletRequest request) {
        if (this.domainName != null) {
            return this.domainName;
        }
        if (this.domainNamePattern != null) {
            Matcher matcher = this.domainNamePattern.matcher(request.getServerName());
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private String getCookiePath(HttpServletRequest request) {
        if (this.cookiePath == null) {
            return request.getContextPath() + "/";
        }
        return this.cookiePath;
    }

    private boolean isServlet3() {
        try {
            ServletRequest.class.getMethod("startAsync");
            return true;
        } catch (NoSuchMethodException e) {
        }
        return false;
    }
}
