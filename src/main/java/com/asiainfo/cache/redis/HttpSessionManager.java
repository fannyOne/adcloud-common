package com.asiainfo.cache.redis;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface HttpSessionManager {

    String getCurrentSessionAlias(HttpServletRequest request);

    Map<String, String> getSessionIds(HttpServletRequest request);

    String encodeURL(String url, String sessionAlias);

    String getNewSessionAlias(HttpServletRequest request);
}
