package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.List;

/**
 * Function:
 * Author: Norman
 * Date: 2017/3/31.
 */
@Data
public class AdNetAptDetailPojoExt extends Pojo {
    private String proName;
    private List<String> allPackages;
}
