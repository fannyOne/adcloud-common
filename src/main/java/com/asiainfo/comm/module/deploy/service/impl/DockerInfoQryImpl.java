package com.asiainfo.comm.module.deploy.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdDockerInfoDAO;
import com.asiainfo.comm.module.models.AdDockerInfo;
import com.asiainfo.comm.module.models.functionModels.DokerObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by zhangpeng on 2016/7/21.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class DockerInfoQryImpl {
    @Autowired
    AdDockerInfoDAO adDockerInfoDAO;
    @Autowired
    AdBranchDAO adBranchDAO;

    public DockerClient dockerInfo() throws Exception {
        DockerClient docker = DefaultDockerClient.fromEnv().build();
        log.error("docker====" + docker);
        return docker;
    }

    public int startContainers(String name, int branch) throws Exception {
        DockerClient docker = dockerInfo();
        String jsonInString = adBranchDAO.getEnvById(branch).getDockerCommand();
        DokerObject obj = new ObjectMapper().readValue(jsonInString, DokerObject.class);

        Iterator<String> it = obj.getExposedPorts().iterator();
        String extPort = "";
        if (it.hasNext()) {
            extPort = it.next();
        }
        PortBinding pb = obj.getHostConfig().getPortBindings().get(extPort).get(0);
        final Map<String, List<PortBinding>> portBindings2 = new HashMap<String, List<PortBinding>>();
        for (String port : obj.getExposedPorts()) {
            List<PortBinding> hostPorts = new ArrayList<PortBinding>();
            hostPorts.add(PortBinding.of(pb.hostIp(), pb.hostPort()));
            portBindings2.put(port, hostPorts);
        }
        ContainerConfig containerConfig2
            = ContainerConfig.builder()
            .image(name).exposedPorts(obj.getExposedPorts())
            .cmd(obj.getCmd())
            .env(obj.getEnv())
            .hostConfig(HostConfig.builder().portBindings(portBindings2).build()).build();

        String jsonResult = new ObjectMapper().writeValueAsString(containerConfig2);
        log.error("转换后的JSON:" + jsonResult);
        System.out.println("=====:" + jsonResult.equals(jsonInString));
        /**
         * Json转Object再转ContainerConfig end
         */

        System.out.println("returnObjJson:" + new JSONArray().fromObject(obj));
        ContainerCreation creation = docker.createContainer(containerConfig2);
        String id = creation.id();
        System.out.println("id===" + id);

        log.error("启动容器");
        docker.startContainer(id);
        log.error("containers===" + id);

        if (id != null && StringUtils.isNotEmpty(id)) {
            System.out.println("================================================");
            adDockerInfoDAO.startContainersStatus(name, id, branch);
            log.error("start  success");
            return 0;
        } else {
            log.error("start faild!!!!!!!!!!!");
            return 1;
        }
    }

    public int stopContainers(String name, int branch) throws Exception {
        DockerClient docker = dockerInfo();
        List<AdDockerInfo> adDockerInfoList = adDockerInfoDAO.qryBranchStatus(name, branch);
        if (adDockerInfoList != null && adDockerInfoList.size() > 0) {
            String id = adDockerInfoList.get(0).getContainersId();
            log.error("停止容器");
            docker.stopContainer(id, 6);
            log.error("删除容器");
            docker.removeContainer(id);
            adDockerInfoDAO.updateContainersStatus(id);
            log.error("停止成功");
            return 0;
        } else {
            log.error("停止失败，没有找到该容器");
            return 1;
        }
    }
}
