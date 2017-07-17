package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

@Data
public class UserPwdPojo extends Pojo {
    String username;
    String email;
    String verificationCode;
    String password;
}
