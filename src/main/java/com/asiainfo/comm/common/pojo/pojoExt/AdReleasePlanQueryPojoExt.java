package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.util.CommandValidator;
import lombok.Data;

/**
 * Created by dlyxn on 2017/5/9.
 */
@Data
public class AdReleasePlanQueryPojoExt extends Pojo {
    private Long groupId;
    private Integer result;
    private String startDate;
    private String endDate;
    private Integer pageNum;

    public void formatCheck() {
        CommandValidator.assertObjectNotNull("groupId", groupId);
        CommandValidator.assertObjectNotNull("result", result);
        CommandValidator.assertObjectNotNull("startDate", startDate);
        CommandValidator.assertObjectNotNull("endDate", endDate);
        CommandValidator.assertObjectNotNull("pageNum", pageNum);
    }
}
