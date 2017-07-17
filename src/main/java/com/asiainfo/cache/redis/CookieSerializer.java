package com.asiainfo.cache.redis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface CookieSerializer {
    void writeCookieValue(CookieValue cookieValue);

    List<String> readCookieValues(HttpServletRequest request);

    class CookieValue {
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final String cookieValue;

        public CookieValue(HttpServletRequest request, HttpServletResponse response,
                           String cookieValue) {
            this.request = request;
            this.response = response;
            this.cookieValue = cookieValue;
        }

        public HttpServletRequest getRequest() {
            return this.request;
        }

        public HttpServletResponse getResponse() {
            return this.response;
        }

        public String getCookieValue() {
            return this.cookieValue;
        }
    }
}
