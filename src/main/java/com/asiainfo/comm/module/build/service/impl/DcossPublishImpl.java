package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * Created by weif on 2016/6/28.
 */
//@Component
public class DcossPublishImpl {

    protected static Logger logger = LoggerFactory.getLogger(DcossPublishImpl.class);
    @Resource
    private RestTemplate restTemplate = new RestTemplate();
    private HttpHeaders headers = new HttpHeaders();


    public DcossPublishImpl(String userName, String passWord) {
        MediaType type = MediaType.parseMediaType("application/json;");
        headers.setContentType(type);
        headers.set("username", userName);
        headers.set("password", passWord);
    }

    public UploadNotifyResult uploadNotify(UploadNotify uploadNotify, String url) {
        HttpEntity request = new HttpEntity(uploadNotify, headers);
        UploadNotifyResult result = restTemplate.postForObject(url, request, UploadNotifyResult.class);
        if (logger.isErrorEnabled()) {
            logger.error("***********上传接口返回" + result.getReturnCode());
        }
        return result;
    }

    public DeployNotifyResult deployNotify(DeployNotify deployNotify, String url) {
        HttpEntity request = new HttpEntity(deployNotify, headers);
        DeployNotifyResult result = restTemplate.postForObject(url, request, DeployNotifyResult.class);
        if (logger.isErrorEnabled()) {
            logger.error("***********发布接口返回" + result.getReturnCode());
        }
        return result;
    }

    public RestartNotifyResult restartNotify(RestartNotify restartNotify, String url) {
        HttpEntity request = new HttpEntity(restartNotify, headers);
        RestartNotifyResult result = restTemplate.postForObject(url, request, RestartNotifyResult.class);
        if (logger.isErrorEnabled()) {
            logger.error("***********重启接口返回" + result.getReturnCode());
        }
        return result;
    }

    public StatusNotifyResult statusNotify(StatusNotify statusNotify, String url) {
        HttpEntity request = new HttpEntity(statusNotify, headers);
        StatusNotifyResult result = restTemplate.postForObject(url, request, StatusNotifyResult.class);
        if (logger.isErrorEnabled()) {
            logger.error("***********查询接口返回" + result.getReturnCode());
        }
        return result;
    }


}
