package com.asiainfo.comm.common.enums;

/**
 * Created by zhangpeng on 2017/5/19.
 */
public enum DcosResultState {

    Finished("Finished", "0"),   // 调用成功
    Deploying("Deploying", "1"),  // 正在部署
    Stopped("Stopped", "2"), // 调用异常
    Failed("Failed", "-1"); // 调用失败

    /**
     * 枚举编号
     */
    private String code;

    /**
     * 枚举详情
     */
    private String value;

    /**
     * 构造方法
     *
     * @param code  枚举编号
     * @param value 枚举详情
     */
    private DcosResultState(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue() {
        this.value = value;
    }
}
