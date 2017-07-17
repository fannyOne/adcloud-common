package com.asiainfo.comm.externalservice.jenkins.jobs;

import com.asiainfo.comm.externalservice.jenkins.auth.User;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.entity.JobConfig;

import java.util.Map;

public interface Job {

    String getName();

    String getScmUrl();

    void setScmUrl(String string);

    void clearNotificationRecipients();

    void addNotificationRecipient(User recipient);

    void removeNotificationRecipient(User recipient);

    String asXml();

    Map getJob();

    JobConfig getJobinfo();
}
