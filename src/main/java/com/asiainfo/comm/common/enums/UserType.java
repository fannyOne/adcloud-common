package com.asiainfo.comm.common.enums;

/**
 * Created by zhenghp on 2016/8/21.
 */
public enum UserType {
    PM(1, "1000"),
    TEST(2, "0100"),
    DEV(3, "0010"),
    DEPLOY(4, "0001");//发布角色

    /**
     * 枚举编号
     */
    private int code;

    /**
     * 枚举详情
     */
    private String description;

    /**
     * 构造方法
     *
     * @param code        枚举编号
     * @param description 枚举详情
     */
    private UserType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserType fromCode(int code) {
        for (final UserType type : UserType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
