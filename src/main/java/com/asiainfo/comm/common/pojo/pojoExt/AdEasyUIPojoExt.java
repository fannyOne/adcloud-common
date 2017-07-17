package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by YangRY
 * 2016/11/11 0011.
 */
@Data
public class AdEasyUIPojoExt {
    private Integer total;
    private List<Map<String, Object>> rows;
}
