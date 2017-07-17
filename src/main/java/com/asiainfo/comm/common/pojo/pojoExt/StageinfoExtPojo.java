package com.asiainfo.comm.common.pojo.pojoExt;

import java.util.List;


public class StageinfoExtPojo {
    private String commitUrl;
    private String commitPerson;
    private String appMachine;
    private String branchPath;
    private String beginTime;
    private String delayTime;
    private String gitUrl;
    private String commitLog;
    private List<DeployPackagesExtPojo> packages;

    public String getCommitLog() {
        return commitLog;
    }

    public void setCommitLog(String commitLog) {
        this.commitLog = commitLog;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public String getCommitPerson() {
        return commitPerson;
    }

    public void setCommitPerson(String commitPerson) {
        this.commitPerson = commitPerson;
    }

    public String getAppMachine() {
        return appMachine;
    }

    public void setAppMachine(String appMachine) {
        this.appMachine = appMachine;
    }

    public String getBranchPath() {
        return branchPath;
    }

    public void setBranchPath(String branchPath) {
        this.branchPath = branchPath;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(String delayTime) {
        this.delayTime = delayTime;
    }

    public List<DeployPackagesExtPojo> getPackages() {
        return packages;
    }

    public void setPackages(List<DeployPackagesExtPojo> packages) {
        this.packages = packages;
    }
}