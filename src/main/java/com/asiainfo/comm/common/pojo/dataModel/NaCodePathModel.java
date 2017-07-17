package com.asiainfo.comm.common.pojo.dataModel;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Function:
 * Author: Norman
 * Date: 2017/4/11.
 */
public class NaCodePathModel {
    private Map map;
    private int pageNum;

    public NaCodePathModel(Map map, int pageNum) {
        this.map = map;
        this.pageNum = pageNum;
    }

    public int getPageNum() {
        return pageNum;
    }

    public NaCodePathModel invoke() throws Exception {
        if (map != null) {
            if (map.containsKey("pageNum") && StringUtils.isNotEmpty(map.get("pageNum").toString())) {
                this.pageNum = Integer.parseInt(map.get("pageNum").toString());
            } else {
                throw new Exception("页数不正确");
            }
        }
        return this;
    }
}
