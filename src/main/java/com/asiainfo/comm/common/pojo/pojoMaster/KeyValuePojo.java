package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.KeyValueExt;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by yangry on 2016/12/28.
 */
@Getter
@Setter
public class KeyValuePojo extends Pojo {
    List<KeyValueExt> keyValues;
}
