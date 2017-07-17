package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.AdEnvPojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/10/21 0021.
 */
@Data
public class AdEnvPojo extends Pojo {
    List<AdEnvPojoExt> envList;
}
