package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * @version v 1.0 on 2016/7/18 15:47
 * @auther william.xu
 */
@XStreamAlias("hudson.plugins.emailext.ExtendedEmailPublisher")
public class ExtendedEmailPublisher extends Notifier {

    public String recipientList = "$DEFAULT_RECIPIENTS";
    public List<EmailTrigger> configuredTriggers = new ArrayList<>();
    public String contentType = "text/html";
    public String defaultSubject = "$DEFAULT_SUBJECT";
    public String defaultContent = "$DEFAULT_CONTENT";
    public String attachmentsPattern;
    public String presendScript;
    public String postsendScript;
    public boolean attachBuildLog;
    public boolean compressBuildLog;
    public String replyTo;
    public boolean saveOutput = false;
    public boolean disabled = false;
    @XStreamAsAttribute
    private String plugin = "email-ext@2.44";

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getRecipientList() {
        return recipientList;
    }

    public void setRecipientList(String recipientList) {
        this.recipientList = recipientList;
    }

    public List<EmailTrigger> getConfiguredTriggers() {
        return configuredTriggers;
    }

    public void setConfiguredTriggers(List<EmailTrigger> configuredTriggers) {
        this.configuredTriggers = configuredTriggers;
    }

    public void addConfiguredTrigger(EmailTrigger emailTrigger) {
        this.configuredTriggers.add(emailTrigger);
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDefaultSubject() {
        return defaultSubject;
    }

    public void setDefaultSubject(String defaultSubject) {
        this.defaultSubject = defaultSubject;
    }

    public String getDefaultContent() {
        return defaultContent;
    }

    public void setDefaultContent(String defaultContent) {
        this.defaultContent = defaultContent;
    }

    public String getAttachmentsPattern() {
        return attachmentsPattern;
    }

    public void setAttachmentsPattern(String attachmentsPattern) {
        this.attachmentsPattern = attachmentsPattern;
    }

    public String getPresendScript() {
        return presendScript;
    }

    public void setPresendScript(String presendScript) {
        this.presendScript = presendScript;
    }

    public String getPostsendScript() {
        return postsendScript;
    }

    public void setPostsendScript(String postsendScript) {
        this.postsendScript = postsendScript;
    }

    public boolean isAttachBuildLog() {
        return attachBuildLog;
    }

    public void setAttachBuildLog(boolean attachBuildLog) {
        this.attachBuildLog = attachBuildLog;
    }

    public boolean isCompressBuildLog() {
        return compressBuildLog;
    }

    public void setCompressBuildLog(boolean compressBuildLog) {
        this.compressBuildLog = compressBuildLog;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public boolean isSaveOutput() {
        return saveOutput;
    }

    public void setSaveOutput(boolean saveOutput) {
        this.saveOutput = saveOutput;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
