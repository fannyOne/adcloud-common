package com.asiainfo.comm.common.pojo.dataModel;

import com.asiainfo.comm.common.pojo.pojoExt.AdReleasePlanQueryPojoExt;

/**
 * Created by dlyxn on 2017/5/15.
 */
public class AdReleasePlanModel {
    private AdReleasePlanQueryPojoExt adReleasePlanQueryPojoExt;
    private int pageNum;

    public AdReleasePlanModel(AdReleasePlanQueryPojoExt adReleasePlanQueryPojoExt, int pageNum) {
        this.adReleasePlanQueryPojoExt = adReleasePlanQueryPojoExt;
        this.pageNum = pageNum;
    }

    public int getPageNum() {
        return pageNum;
    }

    public AdReleasePlanModel invoke() throws Exception {
        if (adReleasePlanQueryPojoExt != null) {
            if (adReleasePlanQueryPojoExt.getPageNum() != null) {
                this.pageNum = adReleasePlanQueryPojoExt.getPageNum();
            } else {
                throw new Exception("页数不正确");
            }
        }
        return this;
    }
}
