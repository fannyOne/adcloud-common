package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.DisOnlineInfo;
import com.asiainfo.comm.common.pojo.pojoExt.OnlineInfo;
import lombok.Data;

import java.util.List;

/**
 * Created by HK on 2016/8/24.
 */
@Data
public class RmpMonthRmpInfoPojo extends Pojo {
    public List<DisOnlineInfo> disOnlineInfoList;
    public List<OnlineInfo> onlineInfoList;
    public int totalNum;
}
