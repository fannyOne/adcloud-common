package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.util.CommandValidator;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by weif on 2016/8/25.
 */
@Data
public class GroupUserExtPojo {

    private Long groupId;

    private List<GroupUserMemberPojo> members;

    public void formatCheck() {
        CommandValidator.assertObjectNotNull("groupId", groupId);
        CommandValidator.assertListNotNull("members", members);
        validMembers(members);
        assertSameUser(members);
    }

    private void validMembers(List<GroupUserMemberPojo> members) {
        for (GroupUserMemberPojo member : members) {
            CommandValidator.assertStringNotNull("userName", member.getUserName());
            assertNotRight(member);
        }
    }

    private void assertNotRight(GroupUserMemberPojo member) {
        if (!member.getPm() && !member.getDev() && !member.getTest() && !member.getDeploy()) {
            throw new IllegalArgumentException(String.format("账号" + member.getUserName() + "的用户角色不能为空", member));
        }
    }

    private void assertSameUser(List<GroupUserMemberPojo> members) {
        List<String> userList = Lists.newArrayList();
        for (GroupUserMemberPojo member : members) {
            for (String userName : userList) {
                if (userName.equals(member.getUserName())) {
                    throw new IllegalArgumentException(String.format("账号" + member.getUserName() + "不可以同时添加", member));
                }
            }
            userList.add(member.getUserName());
        }
    }


}
