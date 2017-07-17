package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by yangry on 2016/12/30.
 */
@Getter
@Setter
public class RmpNumPojo extends Pojo {
    private String onlineDate;
    private Integer countNumber;
    private Integer countImdevelop;
    private Integer countDone;
    private Integer countRollBack;
    private Integer countInvalid;
    private Integer countDevelop;
    private Integer countSubTestTask;
    private String beginDate;
    private Integer[] actualData;
    private Integer alreadyDonNum;

    public RmpNumPojo(String onlineDate
        , Integer countNumber
        , Integer countImdevelop
        , Integer countDone
        , Integer countRollBack
        , Integer countInvalid
        , Integer countDevelop
        , Integer countSubTestTask
        , String beginDate
        , Integer alreadyDonNum
        , Integer[] actualData) {
        this.onlineDate = onlineDate;
        this.countNumber = countNumber;
        this.countImdevelop = countImdevelop;
        this.countDone = countDone;
        this.countRollBack = countRollBack;
        this.countInvalid = countInvalid;
        this.countDevelop = countDevelop;
        this.countSubTestTask = countSubTestTask;
        this.beginDate = beginDate;
        this.actualData = actualData;
        this.alreadyDonNum = alreadyDonNum;
    }
}
