package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdTaskCaseNumberDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zhangpeng on 2016/12/28.
 */
@Component
public class AdTaskCaseNumberImpl {
    @Autowired
    AdTaskCaseNumberDAO adTaskCaseNumberDAO;

    public void deleteByDate(String date) {
        adTaskCaseNumberDAO.deleteByDate(date);
    }

}
