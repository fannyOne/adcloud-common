package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by YangRY
 * 2016/10/26 0026.
 */
@Data
public class FlagPojo extends Pojo {
    private String flag;

    public FlagPojo() {
        this.flag = "true";
    }
}
