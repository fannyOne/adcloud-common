package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/12 0012.
 */
@Data
public class GroupPojoExt {
    private String groupName;
    private long groupId;
    private List<GroupUserMemberPojo> userMember;
    private List<ProjectExtPojo> projects;
}
