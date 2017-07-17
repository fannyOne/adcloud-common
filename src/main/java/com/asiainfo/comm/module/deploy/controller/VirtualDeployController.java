package com.asiainfo.comm.module.deploy.controller;

import com.asiainfo.comm.module.deploy.service.impl.VirtualDeployInfoImpl;
import com.asiainfo.comm.module.models.AdVirtualEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by guojian on 9/28/16.
 */
@RestController
@RequestMapping("/virtualDeploy")
public class VirtualDeployController {

    @Autowired
    private VirtualDeployInfoImpl virtualDeployInfoImpl;

    @RequestMapping(value = "/findByProjectId", produces = "application/json")
    public List<AdVirtualEnvironment> findByProjectId(@RequestParam("projectId") Long projectId) {
        return virtualDeployInfoImpl.findByProjectId(projectId);
    }

    @RequestMapping(value = "/deployVirturl")
    public void deployVirturl(@RequestParam("projectId") Long projectId, @RequestParam("virtualId") Long virtualId) {
        // virtualDeployInfoImpl.deployVirturl(projectId, virtualId);
    }
}
