package com.asiainfo.comm.common.enums;

/**
 * Created by dlyxn on 2017/5/24.
 */
public enum ReleasePlanResultEnum {
    //计划动态，0-失败，1-成功，2-待执行，3-运行中，4-删除
    FAIL(0), SUCCESS(1), WAIT(2), RUN(3), DELETE(4);
    private Integer value;

    ReleasePlanResultEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
