package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by zhenghp on 2016/8/26.
 */
@Data
public class GroupUserMemberPojo {

    private Long userId;

    private String userName;

    private Boolean pm = false;

    private Boolean test = false;

    private Boolean dev = false;

    private Boolean deploy = false; // true表示有发布权限

}
