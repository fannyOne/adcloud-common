package com.asiainfo.comm.common.enums;

/**
 * Created by dlyxn on 2017/5/27.
 */
public enum EvnTypeEnum {
    VM(0), DCOS(1);
    private Integer value;

    EvnTypeEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
