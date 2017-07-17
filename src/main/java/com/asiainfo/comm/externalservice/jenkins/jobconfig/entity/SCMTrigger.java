package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @version v 1.0 on 2016/7/13 17:12
 * @auther william.xu
 */
@XStreamAlias("hudson.triggers.SCMTrigger")
public class SCMTrigger extends Trigger {

    private boolean ignorePostCommitHooks;

    public SCMTrigger() {
    }

    public SCMTrigger(String spec) {
        setSpec(spec);
    }

    public boolean isIgnorePostCommitHooks() {
        return ignorePostCommitHooks;
    }

    public void setIgnorePostCommitHooks(boolean ignorePostCommitHooks) {
        this.ignorePostCommitHooks = ignorePostCommitHooks;
    }
}
