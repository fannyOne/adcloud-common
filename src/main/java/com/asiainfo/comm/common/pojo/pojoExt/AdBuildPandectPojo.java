package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by Administrator on 2017/1/3.
 */
@Data
public class AdBuildPandectPojo extends Pojo {
    long groupNum;
    long branchNum;
    long fileNum;
    long buildNum;
    long avgBuildNum;
}
