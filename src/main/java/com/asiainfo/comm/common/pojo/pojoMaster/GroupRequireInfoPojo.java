package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by YangRY
 * 2016/10/26 0026.
 */
@Data
public class GroupRequireInfoPojo extends Pojo {
    private String groupId;
    private String requireFinish;
    private String requireCount;
    private String requirePre;
    private String developFinish;
    private String developCount;
    private String developPre;
}
