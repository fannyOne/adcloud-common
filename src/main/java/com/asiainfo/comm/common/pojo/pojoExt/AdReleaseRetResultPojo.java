package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.List;

/**
 * Created by dlyxn on 2017/5/26.
 */
@Data
public class AdReleaseRetResultPojo extends Pojo {
    private List<AdReleasePlanPojoExt> plans;
    private Integer total;
}
