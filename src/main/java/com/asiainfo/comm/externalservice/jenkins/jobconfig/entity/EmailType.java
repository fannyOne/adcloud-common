package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @version v 1.0 on 2016/7/18 16:10
 * @auther william.xu
 */
public class EmailType {

    /**
     * A recipient list for only this email type.
     */
    private String recipientList;

    /**
     * The subject of the email
     */
    private String subject;

    /**
     * The body of the email
     */
    private String body;

    /**
     * The list of configured recipient providers
     */
    private List<RecipientProvider> recipientProviders;

    /**
     * Pattern for attachments to be sent as part of this email type.
     */
    private String attachmentsPattern;

    /**
     * True to attach the build log to the email
     */
    private boolean attachBuildLog;

    /**
     * True to compress the build log before attaching it to the email
     */
    private boolean compressBuildLog;

    /**
     * List of email addresses to put into the Reply-To header
     */
    private String replyTo;

    /**
     * Content type to send the email as (HTML or Plaintext)
     */
    private String contentType;

    /**
     * Specifies whether or not we should send this email to the developer/s who
     * made changes.
     */
    private transient boolean sendToDevelopers;

    /**
     * Specifies whether or not we should send this email to the requester who
     * triggered build.
     */
    private transient boolean sendToRequester;

    /**
     * Specifies whether or not we should send this email to all developers
     * since the last success.
     */
    private transient boolean includeCulprits;

    /**
     * Specifies whether or not we should send this email to the recipient list
     */
    private transient boolean sendToRecipientList;

    public EmailType() {
        subject = "$PROJECT_DEFAULT_SUBJECT";
        body = "$PROJECT_DEFAULT_CONTENT";
        //recipientList = "";
        attachmentsPattern = "";
        attachBuildLog = false;
        compressBuildLog = false;
        replyTo = "$PROJECT_DEFAULT_REPLYTO";
        contentType = "project";
        recipientProviders = new ArrayList<>();
    }

    public String getRecipientList() {
        return recipientList;
    }

    public void setRecipientList(String recipientList) {
        this.recipientList = recipientList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<RecipientProvider> getRecipientProviders() {
        return recipientProviders;
    }

    public void setRecipientProviders(List<RecipientProvider> recipientProviders) {
        this.recipientProviders = recipientProviders;
    }

    public String getAttachmentsPattern() {
        return attachmentsPattern;
    }

    public void setAttachmentsPattern(String attachmentsPattern) {
        this.attachmentsPattern = attachmentsPattern;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isSendToDevelopers() {
        return sendToDevelopers;
    }

    public void setSendToDevelopers(boolean sendToDevelopers) {
        this.sendToDevelopers = sendToDevelopers;
    }

    public boolean isSendToRequester() {
        return sendToRequester;
    }

    public void setSendToRequester(boolean sendToRequester) {
        this.sendToRequester = sendToRequester;
    }

    public boolean isIncludeCulprits() {
        return includeCulprits;
    }

    public void setIncludeCulprits(boolean includeCulprits) {
        this.includeCulprits = includeCulprits;
    }

    public boolean isSendToRecipientList() {
        return sendToRecipientList;
    }

    public void setSendToRecipientList(boolean sendToRecipientList) {
        this.sendToRecipientList = sendToRecipientList;
    }
}
