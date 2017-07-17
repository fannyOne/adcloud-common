package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdUserDataRelateDAO;
import com.asiainfo.comm.module.models.AdUserDataRelate;
import com.asiainfo.comm.module.models.functionModels.AdUserDataRelateSimple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by YangRY on 2016/11/9.
 */
@Component
public class AdUserDataRelateImpl {
    @Autowired
    AdUserDataRelateDAO userDataRelateDAO;

    public void updateRel(Long userId, Integer dataType, Integer reportType, String idStr) {
        if (idStr == null || idStr.equals("")) {
            userDataRelateDAO.delete(userId, dataType, reportType);
            return;
        }
        String[] idStrList = idStr.split(",");
        Date date = new Date();
        HashSet<Long> newIdSet = new HashSet<>();
        for (String id : idStrList) {
            newIdSet.add(Long.parseLong(id));
        }
        List<AdUserDataRelateSimple> relateList = userDataRelateDAO.qryByParaNoStateSimple(userId, dataType, reportType);
        List<AdUserDataRelateSimple> updateList = new ArrayList<>();
        if (relateList != null) {
            for (int i = 0; i < relateList.size(); i++) {
                if (relateList.get(i).getState() == 1) {
                    if (newIdSet.contains(relateList.get(i).getForeignId())) {
                        newIdSet.remove(relateList.get(i).getForeignId());
                    } else {
                        relateList.get(i).setState(0);
                        relateList.get(i).setDoneDate(date);
                        updateList.add(relateList.get(i));
                    }
                } else if (newIdSet.contains(relateList.get(i).getForeignId())) {
                    relateList.get(i).setState(1);
                    relateList.get(i).setDoneDate(date);
                    newIdSet.remove(relateList.get(i).getForeignId());
                    updateList.add(relateList.get(i));
                }
            }
        }
        Iterator ite = newIdSet.iterator();
        while (ite.hasNext()) {
            Long newId = (Long) ite.next();
            AdUserDataRelateSimple rel = new AdUserDataRelateSimple();
            rel.setDataType(dataType);
            rel.setUserId(userId);
            rel.setForeignId(newId);
            rel.setReportType(reportType);
            updateList.add(rel);
        }
        userDataRelateDAO.saveSimple(updateList);
    }

    public List<AdUserDataRelate> qryByPara(Long userId, Integer dataType, Integer reportType) {
        return userDataRelateDAO.qryByPara(userId, dataType, reportType);
    }

}
