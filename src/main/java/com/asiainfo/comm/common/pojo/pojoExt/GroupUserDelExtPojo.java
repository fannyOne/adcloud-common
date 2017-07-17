package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.util.CommandValidator;
import lombok.Data;

/**
 * Created by weif on 2016/8/25.
 */
@Data
public class GroupUserDelExtPojo {

    private Long groupId;
    private String userName;
    public void formatCheck() {
        CommandValidator.assertObjectNotNull("groupId", groupId);
        CommandValidator.assertObjectNotNull("userName", userName);
    }

}
