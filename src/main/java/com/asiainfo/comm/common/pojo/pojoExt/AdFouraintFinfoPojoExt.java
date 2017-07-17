package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;


@Data
@XmlRootElement(name="FOURAINTFINFO")
public class AdFouraintFinfoPojoExt {
    private AdFourInfoHeadPojoExt HEAD;
    private AdFourInfoBodyPojoExt BODY;
}
