package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.enums.Authorization;
import lombok.Data;

import java.util.List;

/**
 * Created by zhenghp on 2016/9/9.
 */
@Data
public class GroupAuthorizationPojo {
    Long groupId;
    List<Authorization> authorizations;
}
