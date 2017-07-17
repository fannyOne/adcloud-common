package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by zhangpeng on 2016/10/19.
 */
@Data
public class vmEnvInfoPojoExt extends Pojo {
    private String virtualName;
    private String serverUrl;
    private String serverUsername;
    private String serverPassword;
    private String filePath;
    private String fileName;
    private String packageName;
    private String sourceAddress;
    private String destinationAddress;
    private Integer region;
    private String branchIds;
}
