package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/7.
 */
@Data
public class AdBuildTriggerPojoExt extends Pojo {
    private List<Map<String, String>> branchList;
}
