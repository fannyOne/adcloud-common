package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.GitUserPojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/13 0013.
 */
@Data
public class UsersPojo extends Pojo {
    private long total;
    private List<GitUserPojoExt> users;
}
