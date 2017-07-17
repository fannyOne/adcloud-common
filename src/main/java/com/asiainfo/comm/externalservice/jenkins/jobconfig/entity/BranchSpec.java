package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @version v 1.0 on 2016/7/13 16:47
 * @auther william.xu
 */
@XStreamAlias("hudson.plugins.git.BranchSpec")
public class BranchSpec {
    private String name;

    public BranchSpec(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
