package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdBuildLogDAO;
import com.asiainfo.comm.module.build.dao.impl.AdPipeLineStateDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDAO;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdPipeLineState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by admin on 2016/6/13.
 */
@Component
public class ProjectStateImpl {


    @Autowired
    AdBuildLogDAO buildInfoSinkDAO;

    @Autowired
    AdPipeLineStateDAO adPipeLineStateDAO;
    @Autowired
    AdProjectDAO adProjectDAO;
    @Autowired
    AdBranchDAO branchDAO;

    public Map qryFiveBuildResult(long sysId) {
        int i = 0;
        Map hmap;
        Map envMap = new LinkedHashMap<String, String>();
        Map retMap = new HashMap<String, String>();
        List<AdPipeLineState> adBranchList = adPipeLineStateDAO.qryAllPipeLine();
        for (AdPipeLineState adBranch : adBranchList) {
            if (adBranch.getAdProject().getProjectId().equals(sysId)) {
                envMap.put("" + adBranch.getAdBranch().getBranchId(), adBranch.getAdBranch().getBranchName());
            }
        }
        List<Map> buildLogList = null;
        Map<String, String> systeMap = adProjectDAO.getSystems();
        List<Map> logList = new ArrayList<Map>();
        Map<String, List<Map>> adbuildInfoMap = buildInfoSinkDAO.qryBuildLog(adBranchList);
        if (adbuildInfoMap != null) {
            for (Map.Entry<String, List<Map>> entry : adbuildInfoMap.entrySet()) {
                hmap = new HashMap<String, String>();
                buildLogList = entry.getValue();
                if (buildLogList != null) {
                    Collections.reverse(buildLogList);
                }
                hmap.put("name", envMap.get("" + entry.getKey()));
                hmap.put("branchid", "" + entry.getKey());
                hmap.put("buildHis", buildLogList);
                logList.add(hmap);
            }
        }
        List<Map> retList = new ArrayList<Map>();
        Iterator it = envMap.entrySet().iterator();
        Map inputMap = null;
        Map logMap = null;
        while (it.hasNext()) {
            Map.Entry entity = (Map.Entry) it.next();
            inputMap = new HashMap<String, String>();
            for (Map log : logList) {
                if (log.get("branchid").equals(entity.getKey())) {
                    inputMap = log;
                }
            }
            if (inputMap.size() > 0) {
                retList.add(inputMap);
            } else {
                logMap = new HashMap<String, String>();
                logMap.put("name", "" + entity.getValue());
                logMap.put("branchid", "" + entity.getKey());
                retList.add(logMap);
            }
        }
        if (retList != null) {
            Collections.reverse(retList);
        }
        retMap.put("name", systeMap.get("" + sysId));
        retMap.put("pipeline", retList);
        return retMap;
    }

    public Map qryFiveBuildResultByBranch(long branchId) {
        AdBranch branch = branchDAO.qryBranchByid(branchId);
        Map hmap;
        Map envMap = new LinkedHashMap<String, String>();
        Map retMap = new HashMap<String, String>();
        List<AdPipeLineState> adBranchList = adPipeLineStateDAO.qryByBranch(branchId);
        for (AdPipeLineState adBranch : adBranchList) {
            envMap.put("" + adBranch.getAdBranch().getBranchId(), adBranch.getAdBranch().getBranchName());
        }
        List<Map> buildLogList;
        Map<String, String> systeMap = adProjectDAO.getSystems();
        List<Map> logList = new ArrayList<>();
        Map<String, List<Map>> adbuildInfoMap = buildInfoSinkDAO.qryBuildLog(adBranchList);
        if (adbuildInfoMap != null) {
            for (Map.Entry<String, List<Map>> entry : adbuildInfoMap.entrySet()) {
                hmap = new HashMap<String, String>();
                buildLogList = entry.getValue();
                if (buildLogList != null) {
                    Collections.reverse(buildLogList);
                }
                hmap.put("name", envMap.get("" + entry.getKey()));
                hmap.put("branchid", "" + entry.getKey());
                hmap.put("buildHis", buildLogList);
                logList.add(hmap);
            }
        }
        List<Map> retList = new ArrayList<>();
        Iterator it = envMap.entrySet().iterator();
        Map inputMap;
        Map logMap;
        while (it.hasNext()) {
            Map.Entry entity = (Map.Entry) it.next();
            inputMap = new HashMap<String, String>();
            for (Map log : logList) {
                if (log.get("branchid").equals(entity.getKey())) {
                    inputMap = log;
                }
            }
            if (inputMap.size() > 0) {
                retList.add(inputMap);
            } else {
                logMap = new HashMap<String, String>();
                logMap.put("name", "" + entity.getValue());
                logMap.put("branchid", "" + entity.getKey());
                retList.add(logMap);
            }
        }
        if (retList != null) {
            Collections.reverse(retList);
        }
        retMap.put("name", systeMap.get("" + branch.getAdProject().getProjectId()));
        retMap.put("pipeline", retList);
        return retMap;
    }
}
