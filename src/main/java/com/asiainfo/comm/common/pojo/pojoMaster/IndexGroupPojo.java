package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.IndexGroupInfoPojoExt;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/27.
 */
@Data
public class IndexGroupPojo extends Pojo {
    List<IndexGroupInfoPojoExt> system = new ArrayList<>();

}
