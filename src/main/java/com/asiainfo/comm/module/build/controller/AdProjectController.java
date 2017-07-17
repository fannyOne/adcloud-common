package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.common.pojo.pojoMaster.IndexGroupPojo;
import com.asiainfo.comm.module.build.service.impl.AdGroupImpl;
import com.asiainfo.util.JsonpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by YangRy on 2016/7/27.
 * Param "projectId" is necessary in this rote
 */
@RestController
@lombok.extern.slf4j.Slf4j
@RequestMapping(value = "/project")
public class AdProjectController {
    @Autowired
    AdGroupImpl groupImpl;


    @RequestMapping(value = "/qryIndex", produces = "application/json")
    public String qryIndexGroup() {
        IndexGroupPojo pojMaster = new IndexGroupPojo();
        pojMaster = groupImpl.qryIndexGroup(pojMaster);
        return JsonpUtil.modelToJson(pojMaster);
    }

}
