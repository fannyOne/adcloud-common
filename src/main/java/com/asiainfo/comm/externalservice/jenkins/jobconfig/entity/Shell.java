package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @version v 1.0 on 2016/7/13 18:03
 * @auther william.xu
 */
@XStreamAlias("hudson.tasks.Shell")
public class Shell {
    private String command;

    public Shell() {
    }

    public Shell(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
