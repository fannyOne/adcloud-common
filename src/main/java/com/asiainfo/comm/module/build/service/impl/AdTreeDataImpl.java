package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.AdTreeDataPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.AdTreeDataPojo;
import com.asiainfo.comm.module.build.dao.impl.AdGroupDAO;
import com.asiainfo.comm.module.build.dao.impl.AdTreeDataDAO;
import com.asiainfo.comm.module.build.dao.impl.AdUserDataRelateDAO;
import com.asiainfo.comm.module.models.AdTreeData;
import com.asiainfo.comm.module.models.AdUserDataRelate;
import com.avaje.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by YangRY on 2016/11/9.
 */
@Component
public class AdTreeDataImpl {
    @Autowired
    AdTreeDataDAO treeDataDAO;
    @Autowired
    AdGroupDAO groupDAO;
    @Autowired
    AdUserDataRelateDAO userDataRelateDAO;

    //递归存入目录树的值
    public AdTreeDataPojoExt getData(AdTreeData data, HashSet<Long> param) {
        AdTreeDataPojoExt poj = new AdTreeDataPojoExt();
        if (data.getAdTreeDataList() != null && data.getAdTreeDataList().size() > 0) {
            boolean check = true;
            List<AdTreeDataPojoExt> extList = new ArrayList<>();
            for (AdTreeData dataExt : data.getAdTreeDataList()) {
                AdTreeDataPojoExt dataExtPoj = getData(dataExt, param);
                check = check & dataExtPoj.isCheck();
                extList.add(dataExtPoj);
            }
            poj.setCheck(check);
            poj.setChildren(extList);
        } else {
            poj.setCheck(param.contains(data.getId()));
        }
        poj.setTreeCode(data.getTreeCode());
        poj.setTreeId(data.getId());
        poj.setTreeName(data.getTreeName());
        poj.setTreePara(data.getTreePara());
        return poj;
    }


    public AdTreeDataPojo qryTreeData(Integer treeType, Long userId, Integer reportType) {
        List<AdTreeData> dataList = treeDataDAO.qryByTreeType(treeType);
        List<AdUserDataRelate> relList = userDataRelateDAO.qryByPara(userId, 1, reportType);
        HashSet<Long> params = new HashSet<>();
        for (AdUserDataRelate rel : relList) {
            params.add(rel.getForeignId());
        }
        AdTreeDataPojo poj = new AdTreeDataPojo();
        List<AdTreeDataPojoExt> extList = new ArrayList<>();
        for (AdTreeData data : dataList) {
            AdTreeDataPojoExt ext = getData(data, params);
            extList.add(ext);
        }
        poj.setDataList(extList);
        return poj;
    }

    public AdTreeDataPojo qryGroupData(Long userId, Boolean isAdmin, Integer reportType) {
        AdTreeDataPojo poj = new AdTreeDataPojo();
        List<AdTreeDataPojoExt> extList = new ArrayList<>();
        List<SqlRow> rowList;
        if (isAdmin) {
            rowList = groupDAO.qryGroupSignAdmin(2, userId, reportType);
        } else {
            rowList = groupDAO.qryGroupSignUser(2, userId, reportType);
        }
        if (rowList == null) {
            return poj;
        }
        addTreeDataPojoExt(extList, rowList);
        poj.setDataList(extList);
        return poj;
    }

    public void addTreeDataPojoExt(List<AdTreeDataPojoExt> extList, List<SqlRow> rowList) {
        for (SqlRow sqlRow : rowList) {
            AdTreeDataPojoExt ext = new AdTreeDataPojoExt();
            ext.setCheck(sqlRow.getInteger("UR_STATE") != null);
            ext.setTreeId(sqlRow.getLong("GROUP_ID"));
            ext.setTreeName(sqlRow.getString("GROUP_NAME"));
            extList.add(ext);
        }
    }


    public AdTreeDataPojo qrySonarGroup(Long userId, Boolean isAdmin, Integer reportType) {
        AdTreeDataPojo poj = new AdTreeDataPojo();
        List<AdTreeDataPojoExt> extList = new ArrayList<>();
        List<SqlRow> rowList;
        if (isAdmin) {
            rowList = groupDAO.qrySonarGroup(userId, 2, reportType);
        } else {
            poj.setRetMessage("暂时不支持该选项");
            poj.setRetCode("500");
            return poj;
        }
        if (rowList == null) {
            return poj;
        }
        addTreeDataPojoExt(extList, rowList);
        poj.setDataList(extList);
        return poj;
    }

    public Pojo qryProjectData() {
        AdTreeDataPojo poj = new AdTreeDataPojo();
        poj.setDataList(null);
        poj.setRetMessage("暂时不支持该选项");
        poj.setRetCode("500");
        return poj;
    }
    
}
