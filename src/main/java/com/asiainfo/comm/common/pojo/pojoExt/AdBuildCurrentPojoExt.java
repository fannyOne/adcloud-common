package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by Administrator on 2017/1/4.
 */
@Data
public class AdBuildCurrentPojoExt extends Pojo {
    private String userName;
    private String groupName;
    private String branchName;
    private String buildType;
    private String buildResult;
    private String buildDate;

}
