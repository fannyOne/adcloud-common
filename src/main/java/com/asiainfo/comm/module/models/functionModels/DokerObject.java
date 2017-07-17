package com.asiainfo.comm.module.models.functionModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhangpeng on 2016/7/21.
 */
@Data
public class DokerObject {
    @JsonProperty("Hostname")
    private String hostname;
    @JsonProperty("Domainname")
    private String domainname;
    @JsonProperty("User")
    private String user;
    @JsonProperty("AttachStdin")
    private Boolean attachStdin;
    @JsonProperty("AttachStdout")
    private Boolean attachStdout;
    @JsonProperty("AttachStderr")
    private Boolean attachStderr;
    @JsonProperty("PortSpecs")
    private List<String> portSpecs;
    @JsonProperty("ExposedPorts")
    private Set<String> exposedPorts;
    @JsonProperty("Tty")
    private Boolean tty;
    @JsonProperty("OpenStdin")
    private Boolean openStdin;
    @JsonProperty("StdinOnce")
    private Boolean stdinOnce;
    @JsonProperty("Env")
    private List<String> env;
    @JsonProperty("Cmd")
    private List<String> cmd;
    @JsonProperty("Image")
    private String image;
    @JsonProperty("Volumes")
    private Set<String> volumes;
    @JsonProperty("WorkingDir")
    private String workingDir;
    @JsonProperty("Entrypoint")
    private List<String> entrypoint;
    @JsonProperty("NetworkDisabled")
    private Boolean networkDisabled;
    @JsonProperty("OnBuild")
    private List<String> onBuild;
    @JsonProperty("Labels")
    private Map<String, String> labels;
    @JsonProperty("MacAddress")
    private String macAddress;
    @JsonProperty("HostConfig")
    private HostConfigObject hostConfig;
    @JsonProperty("StopSignal")
    private String stopSignal;

}
