package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by weif on 2017/6/2.
 */
@Data
@XmlRootElement(name = "FouraintfInfo")
public class FouraintfInfo {
    String RETURN_VALUE = "0";
    String ERR_DESC = "";
}
