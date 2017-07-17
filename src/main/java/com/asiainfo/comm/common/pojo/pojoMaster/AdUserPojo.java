package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.module.models.AdUser;
import lombok.Data;

import java.util.List;

/**
 * Created by zhenghp on 2016/8/10.
 */
@Data
public class AdUserPojo extends Pojo {
    private List<AdUser> adUsers;
}
