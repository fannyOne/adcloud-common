package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.GroupAdminPojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by zhenghp on 2016/12/20.
 */
@Data
public class AdGroupAdminUserPojo extends Pojo {
    List<GroupAdminPojoExt> groupAdminPojoExt;
}
