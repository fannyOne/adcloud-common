package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by weif on 2017/4/25.
 */
@Data
public class EnvHttpMonitorPojoExt {

    int appType;//1、db 2、web 3、app

    String jkUrl;

    int dbType;     //1、oracle 2、mysql

    String dbName;

    String dbUserName;

    String dbUserPassword;

    int requestType; //请求方式

    String requestMessage; //请求报文

    String requestBody;


}
