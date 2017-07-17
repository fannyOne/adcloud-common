package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yangry on 2016/12/30.
 */
@Getter
@Setter
public class RmpHisNumPojoExt {
    private String onlineDate;
    private Integer countNum;
    private Integer invalidNum;
    private Integer rollbackNum;
    private Integer rollbackSafelyNum;
    private Integer closedNum;
    private Integer developNum;
    private Integer inDevelopNum;

    public RmpHisNumPojoExt(String onlineDate
        , Integer countNum
        , Integer invalidNum
        , Integer rollbackNum
        , Integer rollbackSafelyNum
        , Integer closedNum
        , Integer developNum
        , Integer inDevelopNum) {
        this.onlineDate = onlineDate;
        this.countNum = countNum;
        this.invalidNum = invalidNum;
        this.rollbackNum = rollbackNum;
        this.rollbackSafelyNum = rollbackSafelyNum;
        this.closedNum = closedNum;
        this.developNum = developNum;
        this.inDevelopNum = inDevelopNum;
    }
}
