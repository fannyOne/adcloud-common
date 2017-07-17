package com.asiainfo.comm.module.external.service.impl;

import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.comm.module.models.AdStaticData;
import com.asiainfo.comm.module.models.RmpSynValue;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

/**
 * Created by weif on 2017/1/10.
 */
@Component
public class RmpSynImpl {


    AdStaticDataImpl bsStaticDataImpl;

    public void sendBuildResult(boolean buildResult) {
        AdStaticData adStaticData = bsStaticDataImpl.qryStaticDataByCodeValue("rmpjk", "rmpurl");
        String url = "";
        if (adStaticData != null) {
            url = adStaticData.getCodeName();
        }
        RmpSynValue rmpSynValue = new RmpSynValue();
        rmpSynValue.setMD5("defde51cf91e1e03");
        rmpSynValue.setSysname("开发平台");
        rmpSynValue.setMethod("TaskCompileFinish");
        rmpSynValue.setCompileresult(buildResult);
        rmpSynValue.setCompilefinishTime(new Date());
        rmpSynValue.setWorkID("TSK2016120653265701");
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
        headers.setContentType(type);
        HttpEntity request = new HttpEntity(rmpSynValue, headers);
        String result = restTemplate.postForObject(url, request, String.class);
        System.out.println("****" + result);

    }

}
