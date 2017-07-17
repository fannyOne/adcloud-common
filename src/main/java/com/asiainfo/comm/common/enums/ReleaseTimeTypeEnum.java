package com.asiainfo.comm.common.enums;

public enum ReleaseTimeTypeEnum {
    PROMPTLY(0), TIMING(1);
    private Integer value;

    ReleaseTimeTypeEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
