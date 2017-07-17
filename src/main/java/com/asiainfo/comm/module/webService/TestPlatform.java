package com.asiainfo.comm.module.webService;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(targetNamespace = "http://web.webservice.aiga.asiainfo.com/")
public interface TestPlatform {
    @WebMethod(operationName = "runTask")
    public String runTask(@WebParam(name = "buildTaskId") String buildTaskId,
                          @WebParam(name = "developTaskTag") String developTaskTag,
                          @WebParam(name = "environmentId") Integer environmentId,
                          @WebParam(name = "url") String url,
                          @WebParam(name = "sysName") String sysName);

}
