package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yangry on 2016/12/21.
 */
@Getter
@Setter
public class NumberCount {
    int number;

    public NumberCount(int number) {
        this.number = number;
    }

    public void addNumber() {
        this.number++;
    }

    public void subNumber() {
        this.number--;
    }
}
