package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdProject;
import lombok.Data;

import java.util.List;

/**
 * Created by weif on 2017/3/22.
 */
@Data
public class AdVirtualEnvironmentPojo extends Pojo {
    private AdProject adProject;
    private String virtualName;
    private String serverUsername;
    private String serverPassword;
    private String serverUrl;
    private String filePath;
    private String fileName;
    private Long env_type;
    private Long state;
    private AdBranch adBranch;
    private String packageName;
    private String sourceAddress;
    private String destinationAddress;
    private String restartShell;
    private Integer region;
    private List<AdBranchCheckPojoExt> branchs;

}
