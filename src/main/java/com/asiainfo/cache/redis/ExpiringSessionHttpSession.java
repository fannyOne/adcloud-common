package com.asiainfo.cache.redis;

import org.springframework.session.ExpiringSession;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Set;

@SuppressWarnings("deprecation")
class ExpiringSessionHttpSession<S extends ExpiringSession> implements HttpSession {
    private static final Enumeration<String> EMPTY_ENUMERATION = new Enumeration<String>() {
        public boolean hasMoreElements() {
            return false;
        }

        public String nextElement() {
            throw new NoSuchElementException("a");
        }
    };
    private static final HttpSessionContext NOOP_SESSION_CONTEXT = new HttpSessionContext() {
        public HttpSession getSession(String sessionId) {
            return null;
        }

        public Enumeration<String> getIds() {
            return EMPTY_ENUMERATION;
        }
    };
    private final ServletContext servletContext;
    private S session;
    private boolean invalidated;
    private boolean old;

    ExpiringSessionHttpSession(S session, ServletContext servletContext) {
        this.session = session;
        this.servletContext = servletContext;
    }

    public S getSession() {
        return this.session;
    }

    public void setSession(S session) {
        this.session = session;
    }

    public long getCreationTime() {
        checkState();
        return this.session.getCreationTime();
    }

    public String getId() {
        return this.session.getId();
    }

    public long getLastAccessedTime() {
        checkState();
        return this.session.getLastAccessedTime();
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public int getMaxInactiveInterval() {
        return this.session.getMaxInactiveIntervalInSeconds();
    }

    public void setMaxInactiveInterval(int interval) {
        this.session.setMaxInactiveIntervalInSeconds(interval);
    }

    public HttpSessionContext getSessionContext() {
        return NOOP_SESSION_CONTEXT;
    }

    public Object getAttribute(String name) {
        checkState();
        return this.session.getAttribute(name);
    }

    public Object getValue(String name) {
        return getAttribute(name);
    }

    public Enumeration<String> getAttributeNames() {
        checkState();
        return Collections.enumeration(this.session.getAttributeNames());
    }

    public String[] getValueNames() {
        checkState();
        Set<String> attrs = this.session.getAttributeNames();
        return attrs.toArray(new String[0]);
    }

    public void setAttribute(String name, Object value) {
        checkState();
        this.session.setAttribute(name, value);
    }

    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    public void removeAttribute(String name) {
        checkState();
        this.session.removeAttribute(name);
    }

    public void removeValue(String name) {
        removeAttribute(name);
    }

    public void invalidate() {
        checkState();
        this.invalidated = true;
    }

    public boolean isNew() {
        checkState();
        return !this.old;
    }

    public void setNew(boolean isNew) {
        this.old = !isNew;
    }

    private void checkState() {
        if (this.invalidated) {
            throw new IllegalStateException(
                "The HttpSession has already be invalidated.");
        }
    }
}
