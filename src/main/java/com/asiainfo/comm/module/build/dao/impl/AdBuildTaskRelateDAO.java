package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdBuildTaskRelate;
import com.asiainfo.comm.module.models.query.QAdBuildTaskRelate;
import com.avaje.ebean.Ebean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */
@Component
public class AdBuildTaskRelateDAO {

    public void addRelate(String taskCodeBr, int isDev, long buildSeq, String codeListsStr) {
        AdBuildTaskRelate relate = new AdBuildTaskRelate();
        relate.setBuildSeq(buildSeq);
        relate.setCodeList(codeListsStr);
        relate.setCreateDate(new Date());
        relate.setState(1);
        relate.setTbCode(taskCodeBr);
        relate.setTbType(isDev);
        Ebean.save(relate);
    }

    public List<AdBuildTaskRelate> getSeqTaskByPubSeqId(long buildSeq) {
        return new QAdBuildTaskRelate().buildSeq.eq(buildSeq).findList();
    }
}
