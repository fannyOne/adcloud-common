package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.AuthorPojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/11 0011.
 */
@Data
public class AuthorPojo extends Pojo {
    private long roleId;
    private List<AuthorPojoExt> authors;
}
