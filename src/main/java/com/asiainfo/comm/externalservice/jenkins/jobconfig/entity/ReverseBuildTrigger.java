package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @version v 1.0 on 2016/7/13 17:25
 * @auther william.xu
 */
@XStreamAlias("jenkins.triggers.ReverseBuildTrigger")
public class ReverseBuildTrigger extends Trigger {

    private String upstreamProjects;
    private Result threshold;
    private String spec;

    public ReverseBuildTrigger() {

    }

    public ReverseBuildTrigger(String upstreamProjects, Result threshold, String spec) {
        this.upstreamProjects = upstreamProjects;
        this.threshold = threshold;
        this.spec = spec;
    }
}
