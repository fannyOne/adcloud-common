package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.List;

/**
 * Created by SS on 2017/5/9.
 */
@Data
public class AdReleasePlanTemplatePojo extends Pojo {
    Long id;
    Long groupId;
    String releaseModelName;
    String releaseTime; //发布时间
    Long releaseModelAuditor; //审核人ID
    String releaseModelAuditorName;
    List<AdReleasePlanTempStagePojo> releaseNode;
}
