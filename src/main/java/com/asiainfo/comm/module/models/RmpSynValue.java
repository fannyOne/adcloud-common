package com.asiainfo.comm.module.models;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

import java.util.Date;

/**
 * Created by weif on 2017/1/10.
 */
@Data

@XStreamAlias("root")
public class RmpSynValue {

    private String sysname;

    private String method;

    private String MD5;

    private boolean Compileresult;

    private Date compilefinishTime;

    private String workID;
}
