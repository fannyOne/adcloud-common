package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * @version v 1.0 on 2016/7/13 15:43
 * @auther william.xu
 */
@XStreamAlias("project")
public class JobConfig {
    private List actions = new ArrayList();
    private boolean keepDependencies;
    private String description;
    private List<JobProperty> properties = new ArrayList<>();
    private SCM scm = new NullSCM();
    private String assignedNode;
    private boolean canRoam = true;
    private boolean disabled;
    private boolean blockBuildWhenDownstreamBuilding;
    private boolean blockBuildWhenUpstreamBuilding;
    private List<Trigger> triggers = new ArrayList<>();
    private boolean concurrentBuild;
    private List<Shell> builders = new ArrayList<>();
    private List<Publisher> publishers = new ArrayList();
    private List buildWrappers = new ArrayList();

    public String getAssignedNode() {
        return assignedNode;
    }

    public void setAssignedNode(String assignedNode) {
        this.assignedNode = assignedNode;
    }

    public List getActions() {
        return actions;
    }

    public void setActions(List actions) {
        this.actions = actions;
    }

    public boolean isKeepDependencies() {
        return keepDependencies;
    }

    public void setKeepDependencies(boolean keepDependencies) {
        this.keepDependencies = keepDependencies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<JobProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<JobProperty> properties) {
        this.properties = properties;
    }

    public void addProperty(JobProperty jobProperty) {
        this.properties.add(jobProperty);
    }

    public SCM getScm() {
        return scm;
    }

    public void setScm(SCM scm) {
        this.scm = scm;
    }

    public boolean isCanRoam() {
        return canRoam;
    }

    public void setCanRoam(boolean canRoam) {
        this.canRoam = canRoam;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isBlockBuildWhenDownstreamBuilding() {
        return blockBuildWhenDownstreamBuilding;
    }

    public void setBlockBuildWhenDownstreamBuilding(boolean blockBuildWhenDownstreamBuilding) {
        this.blockBuildWhenDownstreamBuilding = blockBuildWhenDownstreamBuilding;
    }

    public boolean isBlockBuildWhenUpstreamBuilding() {
        return blockBuildWhenUpstreamBuilding;
    }

    public void setBlockBuildWhenUpstreamBuilding(boolean blockBuildWhenUpstreamBuilding) {
        this.blockBuildWhenUpstreamBuilding = blockBuildWhenUpstreamBuilding;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<Trigger> triggers) {
        this.triggers = triggers;
    }

    public void addTrigger(Trigger trigger) {
        if (this.triggers == null) {
            this.triggers = new ArrayList<>();
        }
        this.triggers.add(trigger);
    }

    public boolean isConcurrentBuild() {
        return concurrentBuild;
    }

    public void setConcurrentBuild(boolean concurrentBuild) {
        this.concurrentBuild = concurrentBuild;
    }

    public List<Shell> getBuilders() {
        return builders;
    }

    public void setBuilders(List<Shell> builders) {
        this.builders = builders;
    }

    public void addBuilder(Shell shell) {
        this.builders.add(shell);
    }

    public List<Publisher> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<Publisher> publishers) {
        this.publishers = publishers;
    }

    public void addPublisher(Publisher publisher) {
        this.publishers.add(publisher);
    }

    public List getBuildWrappers() {
        return buildWrappers;
    }

    public void setBuildWrappers(List buildWrappers) {
        this.buildWrappers = buildWrappers;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
