package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.common.pojo.pojoExt.AdAllGroupPojoExt;
import com.asiainfo.comm.module.build.service.impl.AdGroupImpl;
import com.asiainfo.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangpeng on 2016/11/30.
 */

@RestController
@RequestMapping(value = "/Monitor")
@Component
public class AdEnvMonitorController {

    @Autowired
    AdGroupImpl adGroupImpl;

    /**
     * 查询所有项目，填充页面下拉框
     *
     * @return 查到的结果
     */
    @RequestMapping(value = "/queryAllGroup", produces = "application/json")
    public String queryAllGroup() {
        Map<String, Object> retMap = new HashMap<>();
        try {
            List<AdAllGroupPojoExt> groups = adGroupImpl.qryAllGroupName();
            retMap.put("allGroupName", groups);
            retMap.put("retCode", 200);
        } catch (Exception e) {
            retMap.put("retCode", 500);
            retMap.put("m", e.getMessage());
        }
        return JsonUtil.mapToJson(retMap);
    }

}

