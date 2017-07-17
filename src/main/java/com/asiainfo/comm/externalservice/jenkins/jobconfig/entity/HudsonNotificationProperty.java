package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * @version v 1.0 on 2016/7/13 16:04
 * @auther william.xu
 */
@XStreamAlias("com.tikal.hudson.plugins.notification.HudsonNotificationProperty")
public class HudsonNotificationProperty extends JobProperty {

    public List<Endpoint> endpoints = new ArrayList<>();
    @XStreamAsAttribute
    private String plugin = "notification@1.10";

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public void addEndpoint(Endpoint endpoint) {
        this.endpoints.add(endpoint);
    }
}
