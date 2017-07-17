package com.asiainfo.comm.module.report.service.impl;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.OperationGroupReportPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.OperationReportPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.GroupReportPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.OperationReportPojo;
import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.build.service.impl.*;
import com.asiainfo.comm.module.models.AdGroup;
import com.asiainfo.comm.module.report.dao.impl.OperationReportDao;
import com.avaje.ebean.SqlRow;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;


/**
 * Created by zhenghp on 2016/11/8.
 */
@lombok.extern.slf4j.Slf4j
@Component
public class OperationReportImpl {
    @Autowired
    OperationReportDao dao;
    @Autowired
    AdUserImpl userImpl;
    @Autowired
    AdGroupImpl groupImpl;
    @Autowired
    AdBranchImpl adBranchImpl;
    @Autowired
    AdUserDataRelateImpl adUserDataRelateImpl;

    @Autowired
    AdOperationImpl adOperationImpl;


    public Pojo collectInfo() {
        OperationReportPojo pojo = new OperationReportPojo();
        OperationReportPojoExt pojoExt = new OperationReportPojoExt();
        pojoExt.setUserCount(userImpl.countUsersCount());
        pojoExt.setLastMonthUserCount(userImpl.countLastMonth());

        pojoExt.setGroupCount(groupImpl.countAllGroup());
        pojoExt.setLastMonthGroupCount(groupImpl.countLastMonthGroup());

        pojoExt.setBranchCount(adBranchImpl.countAll());
        pojoExt.setLastMonthBranchCount(adBranchImpl.countLastMonth());

        pojoExt.setEnvCount(adBranchImpl.countAllEvn());
        pojoExt.setLastMonthEnvCount(adBranchImpl.countLastMonthEnv());
        pojo.setOperationReport(pojoExt);
        return pojo;
    }

    public OperationGroupReportPojoExt qryGroupReport(long userId, String groupIds, String qryTypes) {
        OperationGroupReportPojoExt pojo = new OperationGroupReportPojoExt();
        List<GroupReportPojo> groupinfo = Lists.newArrayList();
        adUserDataRelateImpl.updateRel(userId, CommConstants.USER_DATA_RELATE.AD_GROUP, CommConstants.TREE_DATA.OPERATION_REPORT, groupIds);
        adUserDataRelateImpl.updateRel(userId, CommConstants.USER_DATA_RELATE.AD_TREE_DATA, CommConstants.TREE_DATA.OPERATION_REPORT, qryTypes);
        HashMap<Integer, HashMap<Long, Long>> allReportMap = buildReportInfo(qryTypes);
        List<AdGroup> groups = groupImpl.qryGroupByIds(groupIds);
        if (CollectionUtils.isNotEmpty(groups)) {
            for (AdGroup group : groups) {
                GroupReportPojo gpojo = new GroupReportPojo();
                if (StringUtils.isNotEmpty(qryTypes)) {
                    gpojo.setGorupId(group.getGroupId());
                    gpojo.setGroupName(group.getGroupName());
                    buildPojo(gpojo, qryTypes, allReportMap);
                }
                groupinfo.add(gpojo);
            }
        }
        pojo.setRows(groupinfo);
        pojo.setTotal(Long.valueOf(groupinfo.size()));
        return pojo;
    }

    private void buildPojo(GroupReportPojo gpojo, String qryTypes, HashMap<Integer, HashMap<Long, Long>> map) {
        if (StringUtils.isNotEmpty(qryTypes)) {
            for (String type : qryTypes.split(",")) {
                int itype = Integer.parseInt(type);
                switch (itype) {
                    case 26:// 使用次数
                        setUsedCount(gpojo, map.get(itype));
                        break;
                    case 27://  接入环境
                        setEnvCount(gpojo, map.get(itype));
                        break;
                    case 28://  接入流水
                        setBranch(gpojo, map.get(itype));
                        break;
                    case 29://平均构建时长
                        setAvgTime(gpojo, map.get(itype));
                        break;
                    case 30://用户数
                        setGroupUser(gpojo, map.get(itype));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private HashMap<Integer, HashMap<Long, Long>> buildReportInfo(String qryTypes) {
        HashMap<Integer, HashMap<Long, Long>> map = Maps.newHashMap();
        if (StringUtils.isNotEmpty(qryTypes)) {
            for (String type : qryTypes.split(",")) {
                int itype = Integer.parseInt(type);
                switch (itype) {
                    case 26:// 使用次数
                        map.put(itype, sqlRowToMap(dao.countBuild()));
                        break;
                    case 27://  接入环境
                        map.put(itype, sqlRowToMap(dao.countEnv()));
                        break;
                    case 28://  接入流水
                        map.put(itype, sqlRowToMap(dao.countBranch()));
                        break;
                    case 29://平均构建时长
                        map.put(itype, sqlRowToMap(dao.countBuildAvgTime()));
                        break;
                    case 30://用户数
                        map.put(itype, sqlRowToMap(dao.countGroupUser()));
                        break;
                    default:
                        break;
                }
            }
        }
        return map;
    }

    private void setGroupUser(GroupReportPojo pojo, HashMap<Long, Long> userMap) {
        pojo.setGroupUserCount(userMap.get(pojo.getGorupId()));
    }

    private void setBranch(GroupReportPojo pojo, HashMap<Long, Long> userMap) {
        pojo.setBranchCount(userMap.get(pojo.getGorupId()));
    }

    private void setAvgTime(GroupReportPojo pojo, HashMap<Long, Long> userMap) {
        if (null != userMap.get(pojo.getGorupId()))
            //pojo.setAvgTime(adOperationImpl.tranTime(userMap.get(pojo.getGorupId())));
            pojo.setAvgTime(String.valueOf(userMap.get(pojo.getGorupId())) + "s");
    }

    private void setEnvCount(GroupReportPojo pojo, HashMap<Long, Long> userMap) {
        pojo.setEnvCount(userMap.get(pojo.getGorupId()));
    }

    private void setUsedCount(GroupReportPojo pojo, HashMap<Long, Long> userMap) {
        pojo.setUsedCount(userMap.get(pojo.getGorupId()));
    }

    private HashMap<Long, Long> sqlRowToMap(List<SqlRow> sqlRow) {
        HashMap<Long, Long> map = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(sqlRow)) {
            for (SqlRow row : sqlRow) {
                map.put(row.getLong("groupId"), row.getLong("count"));
            }
        }
        return map;
    }
}
