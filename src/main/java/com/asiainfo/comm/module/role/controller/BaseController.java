package com.asiainfo.comm.module.role.controller;

import com.asiainfo.comm.common.pojo.ErrorPojo;
import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.module.build.service.impl.AdUserImpl;
import com.asiainfo.comm.module.models.AdUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhenghp on 2016/8/26.
 */
@lombok.extern.slf4j.Slf4j
@ControllerAdvice
public class BaseController {
    @Autowired
    AdUserImpl userImpl;

    @ExceptionHandler
    @ResponseBody
    public Pojo processException(HttpServletRequest request, Exception ex) {
        Pojo pojo = new ErrorPojo();
        pojo.setRetMessage(ex.getMessage());
        log.error(ex.getMessage(), ex);
        return pojo;
    }

    @ExceptionHandler(PersistenceException.class)
    @ResponseBody
    public Pojo processPersistenceException(HttpServletRequest request, Exception ex) {
        log.error(ex.getMessage(), ex);
        Pojo pojo = new ErrorPojo();
        pojo.setRetMessage("sql异常请查看后台日志");
        return pojo;
    }

    public String getUserName(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("username");
    }

    public Long getUserId(HttpServletRequest request) {
        return (Long) request.getSession().getAttribute("userId");
    }

    public AdUser getAdUserByUserId(HttpServletRequest request) {
        Long userId = getUserId(request);
        return userImpl.qryById(userId);
    }

}
