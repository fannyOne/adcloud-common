package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.AdTreeDataPojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by YangRY on 2016/11/9.
 */
@Data
public class AdTreeDataPojo extends Pojo {
    List<AdTreeDataPojoExt> dataList;
}
