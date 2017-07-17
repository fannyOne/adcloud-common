package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by weif on 2016/12/28.
 */
@XStreamAlias("hudson.triggers.TimerTrigger")
public class TimerTrigger extends Trigger {

    public TimerTrigger(String spec) {
        setSpec(spec);
    }

}
