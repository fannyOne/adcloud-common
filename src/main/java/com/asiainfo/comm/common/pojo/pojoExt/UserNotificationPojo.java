package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by SS on 2017/4/20.
 */
@Data
public class UserNotificationPojo extends Pojo {
    private boolean sysNotify;
    private boolean emailNotify;
    private boolean smsNotify;
}
