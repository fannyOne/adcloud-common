package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.module.build.service.impl.JenkinsStatusImpl;
import com.asiainfo.comm.module.role.controller.BaseController;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@lombok.extern.slf4j.Slf4j
public class JenkinsJobStatusController extends BaseController {

    private static final String STRING_PAYLOAD = "payload";
    protected static Logger logger = LoggerFactory.getLogger(JenkinsJobStatusController.class);
    public String STRING_WRONG_BODY = "请求报文不正确";
    @Autowired
    JenkinsStatusImpl jenkinsStatus;

    @RequestMapping(
        value = "/jobnotification",
        method = RequestMethod.POST)
    public String jobnotification(@RequestBody String payload) {
        System.out.println("请求报文" + payload);
        if (StringUtils.isEmpty(payload)) {
            return STRING_WRONG_BODY;
        }
        try {
            if (logger.isErrorEnabled()) {
                logger.error("*****请求报文=" + payload);
            }
            jenkinsStatus.dealJobNotification(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //根据jobname获取是否是第一个环节和最后一个环节
        return STRING_PAYLOAD;
    }


    @RequestMapping(value = "/jobNotify",
        method = RequestMethod.POST)
    public String jobNotify(@RequestBody String payload) {
        if (StringUtils.isEmpty(payload)) {
            return STRING_WRONG_BODY;
        }
        try {
            if (logger.isErrorEnabled()) {
                logger.error("The request message from jenkins is:\n" + payload);
            }
            jenkinsStatus.jobNotify(payload);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return STRING_PAYLOAD;
    }

}
