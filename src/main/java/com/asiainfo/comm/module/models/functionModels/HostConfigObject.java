package com.asiainfo.comm.module.models.functionModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.PortBinding;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangpeng on 2016/7/21.
 */
@Data
public class HostConfigObject {
    @JsonProperty("Binds")
    private List<String> binds;
    @JsonProperty("ContainerIDFile")
    private String containerIDFile;
    @JsonProperty("LxcConf")
    private List<HostConfig.LxcConfParameter> lxcConf;
    @JsonProperty("Privileged")
    private Boolean privileged;
    @JsonProperty("PortBindings")
    private Map<String, List<PortBinding>> portBindings;
    @JsonProperty("Links")
    private List<String> links;
    @JsonProperty("PublishAllPorts")
    private Boolean publishAllPorts;
    @JsonProperty("Dns")
    private List<String> dns;
    @JsonProperty("DnsSearch")
    private List<String> dnsSearch;
    @JsonProperty("ExtraHosts")
    private List<String> extraHosts;
    @JsonProperty("VolumesFrom")
    private List<String> volumesFrom;
    @JsonProperty("NetworkMode")
    private String networkMode;
    @JsonProperty("SecurityOpt")
    private List<String> securityOpt;
    @JsonProperty("Memory")
    private Long memory;
    @JsonProperty("MemorySwap")
    private Long memorySwap;
    @JsonProperty("CpuShares")
    private Long cpuShares;
    @JsonProperty("CpusetCpus")
    private String cpusetCpus;
    @JsonProperty("CpuQuota")
    private Long cpuQuota;
    @JsonProperty("CgroupParent")
    private String cgroupParent;
    @JsonProperty("RestartPolicy")
    private HostConfig.RestartPolicy restartPolicy;
    @JsonProperty("LogConfig")
    private LogConfig logConfig;
}
