package com.asiainfo.cache.redis;

import org.springframework.context.ApplicationListener;
import org.springframework.session.ExpiringSession;
import org.springframework.session.events.AbstractSessionEvent;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDestroyedEvent;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.List;

public class SessionEventHttpSessionListenerAdapter
    implements ApplicationListener<AbstractSessionEvent>, ServletContextAware {
    private final List<HttpSessionListener> listeners;

    private ServletContext context;

    public SessionEventHttpSessionListenerAdapter(List<HttpSessionListener> listeners) {
        super();
        this.listeners = listeners;
    }

    public void onApplicationEvent(AbstractSessionEvent event) {
        if (this.listeners.isEmpty()) {
            return;
        }

        HttpSessionEvent httpSessionEvent = createHttpSessionEvent(event);

        for (HttpSessionListener listener : this.listeners) {
            if (event instanceof SessionDestroyedEvent) {
                listener.sessionDestroyed(httpSessionEvent);
            } else if (event instanceof SessionCreatedEvent) {
                listener.sessionCreated(httpSessionEvent);
            }
        }
    }

    private HttpSessionEvent createHttpSessionEvent(AbstractSessionEvent event) {
        ExpiringSession session = event.getSession();
        HttpSession httpSession = new ExpiringSessionHttpSession<ExpiringSession>(session,
            this.context);
        HttpSessionEvent httpSessionEvent = new HttpSessionEvent(httpSession);
        return httpSessionEvent;
    }

    public void setServletContext(ServletContext servletContext) {
        this.context = servletContext;
    }
}
