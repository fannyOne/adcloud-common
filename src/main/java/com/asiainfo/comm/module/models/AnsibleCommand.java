package com.asiainfo.comm.module.models;

import lombok.Data;

/**
 * Created by zhenghp on 2017/1/24.
 */
@Data
public abstract class AnsibleCommand {
    String hostIp;
    String userName;
    String password;
}
