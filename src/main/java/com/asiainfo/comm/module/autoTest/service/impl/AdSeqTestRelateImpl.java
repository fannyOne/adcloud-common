package com.asiainfo.comm.module.autoTest.service.impl;

import com.asiainfo.comm.module.autoTest.dao.impl.AdSeqTestRelateDAO;
import com.asiainfo.comm.module.models.AdSeqTestRelate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by YangRY
 * 2016/7/18 0018.
 */
@Component
public class AdSeqTestRelateImpl {
    @Autowired
    AdSeqTestRelateDAO seqTestRelateDAO;

    public AdSeqTestRelate qryBySeq(long seqId, String testType) throws ParseException {
        AdSeqTestRelate relate;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        if (testType.equals("ALL")) {
            relate = seqTestRelateDAO
                .qryBySeqAndCreate(seqId, new Date(), testType);
            if (relate == null) {
                relate = seqTestRelateDAO.qryBySeqAndCreate(seqId, cal.getTime(), testType);
            }
        } else {
            relate = seqTestRelateDAO.qryBySeq(seqId);
        }
        return relate;
    }

    public AdSeqTestRelate qryBySeq(long seqId) throws ParseException {
        AdSeqTestRelate relate;
        relate = seqTestRelateDAO.qryBySeq(seqId);
        return relate;
    }

    public List<AdSeqTestRelate> qryBySeqAndType(long seqId, String autoType) throws ParseException {
        List<AdSeqTestRelate> relate;
        relate = seqTestRelateDAO.qryBySeqAndType(seqId, autoType);
        return relate;
    }

    public AdSeqTestRelate qryByStageId(long seqId, long stageId) {
        AdSeqTestRelate relate;
        relate = seqTestRelateDAO.qryByStageId(seqId, stageId);
        return relate;
    }

    public AdSeqTestRelate qryBySeqAndCreate(long seqId, Date date, String testType) throws ParseException {
        return seqTestRelateDAO.qryBySeqAndCreate(seqId, date, testType);
    }

    public void save(AdSeqTestRelate relate) {
        seqTestRelateDAO.save(relate);
    }
}
