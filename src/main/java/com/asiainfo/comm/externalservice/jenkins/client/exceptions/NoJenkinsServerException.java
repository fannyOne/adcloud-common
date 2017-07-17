package com.asiainfo.comm.externalservice.jenkins.client.exceptions;

@SuppressWarnings("serial")
public class NoJenkinsServerException extends JenkinsException {

    public NoJenkinsServerException(String endpoint) {
        super("No Jenkins server response for endpoint: " + endpoint);
    }

}
