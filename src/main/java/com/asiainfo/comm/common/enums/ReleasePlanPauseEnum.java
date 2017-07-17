package com.asiainfo.comm.common.enums;

/**
 * 计划暂停值枚举
 */
public enum ReleasePlanPauseEnum {
    /**
     * start 正常
     * pause 暂停
     */
    START(0), PAUSE(1);
    /**
     * 枚举值
     */
    private Integer value;

    ReleasePlanPauseEnum(Integer value) {
        this.value = value;
    }

    /**
     * 判断是否是合法范围
     *
     * @param pause 枚举值
     * @return 返回结果
     */
    public static boolean contain(Integer pause) {
        for (ReleasePlanPauseEnum planPauseEnum : values()) {
            if (pause == null) {
                return false;
            }
            if (pause.equals(planPauseEnum.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * GET方法
     *
     * @return 值
     */
    public Integer getValue() {
        return value;
    }
}
