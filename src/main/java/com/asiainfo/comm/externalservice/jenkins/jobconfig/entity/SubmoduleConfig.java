package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

/**
 * @version v 1.0 on 2016/7/13 16:56
 * @auther william.xu
 */
public class SubmoduleConfig {
    private String submoduleName;
    private String[] branches;

    public String getSubmoduleName() {
        return submoduleName;
    }

    public void setSubmoduleName(String submoduleName) {
        this.submoduleName = submoduleName;
    }

    public String[] getBranches() {
        return branches;
    }

    public void setBranches(String[] branches) {
        this.branches = branches;
    }
}
