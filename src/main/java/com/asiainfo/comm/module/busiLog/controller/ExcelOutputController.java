package com.asiainfo.comm.module.busiLog.controller;

import com.asiainfo.comm.module.busiLog.service.impl.ExcelOutputServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by weif on 2016/11/9.
 */
@RestController
@lombok.extern.slf4j.Slf4j
@RequestMapping(value = "/Excel")
public class ExcelOutputController {

    @Autowired
    ExcelOutputServiceImpl excelOutputService;


    @RequestMapping(value = "/OutputExcel", method = RequestMethod.POST)
    public String OutputExcel(HttpServletResponse response, HttpServletRequest request) throws IOException {
        return "";
    }

}
