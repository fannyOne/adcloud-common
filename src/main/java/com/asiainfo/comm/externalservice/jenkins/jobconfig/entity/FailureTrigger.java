package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @version v 1.0 on 2016/7/18 16:22
 * @auther william.xu
 */
@XStreamAlias("hudson.plugins.emailext.plugins.trigger.FailureTrigger")
public class FailureTrigger extends EmailTrigger {

    public FailureTrigger() {
        super();
    }
}
