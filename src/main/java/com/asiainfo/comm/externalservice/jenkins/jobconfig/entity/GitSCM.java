package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @version v 1.0 on 2016/7/13 16:37
 * @auther william.xu
 */
@XStreamAlias("hudson.plugins.git.GitSCM")
public class GitSCM extends SCM {

    @XStreamAsAttribute
    private String plugin = "git@2.4.0";

    private Long configVersion = 2l;

    private List<UserRemoteConfig> userRemoteConfigs = new ArrayList<>();

    private List<BranchSpec> branches = new ArrayList<>();

    private boolean doGenerateSubmoduleConfigurations;

    private Collection<SubmoduleConfig> submoduleCfg = new ArrayList<>();

    private List<GitSCMExtension> extensions = new ArrayList<>();

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public Long getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(Long configVersion) {
        this.configVersion = configVersion;
    }

    public List<UserRemoteConfig> getUserRemoteConfigs() {
        return userRemoteConfigs;
    }

    public void setUserRemoteConfigs(List<UserRemoteConfig> userRemoteConfigs) {
        this.userRemoteConfigs = userRemoteConfigs;
    }

    public void addUserRemoteConfig(UserRemoteConfig userRemoteConfig) {
        this.userRemoteConfigs.add(userRemoteConfig);
    }

    public List<BranchSpec> getBranches() {
        return branches;
    }

    public void setBranches(List<BranchSpec> branches) {
        this.branches = branches;
    }

    public void addBranches(BranchSpec branchSpec) {
        this.branches.add(branchSpec);
    }

    public boolean isDoGenerateSubmoduleConfigurations() {
        return doGenerateSubmoduleConfigurations;
    }

    public void setDoGenerateSubmoduleConfigurations(boolean doGenerateSubmoduleConfigurations) {
        this.doGenerateSubmoduleConfigurations = doGenerateSubmoduleConfigurations;
    }

    public Collection<SubmoduleConfig> getSubmoduleCfg() {
        return submoduleCfg;
    }

    public void setSubmoduleCfg(Collection<SubmoduleConfig> submoduleCfg) {
        this.submoduleCfg = submoduleCfg;
    }

    public List<GitSCMExtension> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<GitSCMExtension> extensions) {
        this.extensions = extensions;
    }
}
