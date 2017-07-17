package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.GroupAuthorizationPojo;
import lombok.Data;

import java.util.List;

/**
 * Created by zhenghp on 2016/9/9.
 */
@Data
public class UserAuthorizationPojo extends Pojo {
    String userName;
    Long userId;
    List<GroupAuthorizationPojo> groupAuthorization;


}
