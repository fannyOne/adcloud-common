package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.GroupUserMemberPojo;
import lombok.Data;

import java.util.List;

/**
 * Created by zhenghp on 2016/8/26.
 */
@Data
public class GroupUserPojo extends Pojo {
    private long groupId;

    private List<GroupUserMemberPojo> members;
}
