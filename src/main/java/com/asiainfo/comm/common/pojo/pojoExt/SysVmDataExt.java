package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by weif on 2017/1/16.
 */
@Data
public class SysVmDataExt {
    private Long vmId;

    private String serverUrl;

    private String serverUsername;

    private String serverPassword;

    private String filePath;

    private String fileName;

    private String packName;

    private String sourceAddress;

    private String destAddress;


}
