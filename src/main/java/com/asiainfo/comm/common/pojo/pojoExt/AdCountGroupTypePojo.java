package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2017/1/6.
 */
@Data
public class AdCountGroupTypePojo extends Pojo {
    private List<AdCountGroupTypePojoExt> adCountGroupTypePojoExtList;
}
