package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.RmpHisNumPojoExt;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by yangry on 2016/12/30.
 */
@Getter
@Setter
public class RmpHisNumPojo extends Pojo {
    List<RmpHisNumPojoExt> dataList;
}
