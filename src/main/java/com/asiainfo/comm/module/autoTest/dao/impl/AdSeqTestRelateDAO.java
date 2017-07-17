package com.asiainfo.comm.module.autoTest.dao.impl;

import com.asiainfo.comm.module.models.AdSeqTestRelate;
import com.asiainfo.comm.module.models.query.QAdSeqTestRelate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by YangRY
 * 2016/7/18 0018.
 */
@Component
public class AdSeqTestRelateDAO {
    public void save(AdSeqTestRelate relate) {
        relate.save();
    }

    public AdSeqTestRelate qryBySeqAndCreate(long seqId, Date createDate, String testType) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date create;
        if (testType.equals("ALL")) {
            create = sdf.parse(sdf.format(createDate));
        } else {
            create = new Date();
        }
        List<AdSeqTestRelate> relates = new QAdSeqTestRelate().createDate
            .eq(create).seqId.eq(seqId).testType.eq(testType).state.eq(1).findList();
        if (relates != null && relates.size() > 0) {
            return relates.get(0);
        } else {
            return null;
        }
    }

    public AdSeqTestRelate qryBySeq(long seqId) {
        List<AdSeqTestRelate> seqSeqTestRelateList = new QAdSeqTestRelate().state.eq(1)
            .seqId.eq(seqId).orderBy(" CREATE_DATE ASC ").findList();
        if (seqSeqTestRelateList != null && seqSeqTestRelateList.size() > 0) {
            return seqSeqTestRelateList.get(0);
        } else {
            return null;
        }
    }

    public AdSeqTestRelate qryByStageId(long seqId, long stageId) {
        List<AdSeqTestRelate> seqSeqTestRelateList = new QAdSeqTestRelate().seqId.eq(seqId).adStage.stageId.eq(stageId).findList();
        if (seqSeqTestRelateList != null && !seqSeqTestRelateList.isEmpty()) {
            return seqSeqTestRelateList.get(0);
        } else {
            return null;
        }
    }

    public List<AdSeqTestRelate> qryBySeqAndType(long seqId, String autoType) {
        List<AdSeqTestRelate> seqSeqTestRelateList = new QAdSeqTestRelate().state.eq(1)
            .testType.eq(autoType).seqId.eq(seqId).orderBy(" CREATE_DATE DESC ").findList();
        if (!CollectionUtils.isEmpty(seqSeqTestRelateList)) {
            return seqSeqTestRelateList;
        } else {
            return null;
        }
    }
}
