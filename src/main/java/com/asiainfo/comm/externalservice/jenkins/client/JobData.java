package com.asiainfo.comm.externalservice.jenkins.client;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weif on 2016/7/20.
 */
public class JobData {

    private String username;
    private String password;
    private String token;
    private String server;
    private String port;
    private String job;
    private List<NameValuePair> parameters;

    public JobData() {
        parameters = new ArrayList<NameValuePair>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public List<NameValuePair> getParameters() {
        return parameters;
    }
}
