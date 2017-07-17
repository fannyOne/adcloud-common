package com.asiainfo.comm.externalservice.jenkins.client;

import com.asiainfo.comm.externalservice.jenkins.guice.JenkinsUrl;
import com.asiainfo.comm.externalservice.jenkins.guice.JenkinsWsClientGuiceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class JenkinsClientFactory {

    private Injector injector;

    public JenkinsClientFactory(@JenkinsUrl URL jenkinsUrl, String username, String password) {
        checkNotNull(jenkinsUrl, "jenkinsUrl must be non-null");
        checkArgument(isNotEmpty(username), "username must be non-empty");

        injector = Guice.createInjector(new JenkinsWsClientGuiceModule(jenkinsUrl, username, password));
    }

    public JenkinsClient getJenkinsClient() {
        return injector.getInstance(JenkinsClient.class);
    }

}
