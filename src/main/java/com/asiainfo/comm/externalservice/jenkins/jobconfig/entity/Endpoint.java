package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @version v 1.0 on 2016/7/13 16:07
 * @auther william.xu
 */
@XStreamAlias("com.tikal.hudson.plugins.notification.Endpoint")
public class Endpoint {

    private String protocol = "HTTP";
    private String format = "JSON";
    private String url;
    private String event = "all";
    private Integer timeout = 30000;
    private Integer loglines = 0;


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getLoglines() {
        return loglines;
    }

    public void setLoglines(Integer loglines) {
        this.loglines = loglines;
    }
}
