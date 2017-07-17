package com.asiainfo.comm.common.pojo.dataModel;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by yangry on 2017/2/7.
 */
public class BranchInfoPara {
    private Map map;
    private String projects;
    private int pageNum;

    public BranchInfoPara(Map map, String projects, int pageNum) {
        this.map = map;
        this.projects = projects;
        this.pageNum = pageNum;
    }

    public String getProjects() {
        return projects;
    }

    public int getPageNum() {
        return pageNum;
    }

    public BranchInfoPara invoke() throws Exception {
        if (map != null) {
            if (map.containsKey("pageNum") && StringUtils.isNotEmpty((String) map.get("pageNum"))) {
                pageNum = Integer.parseInt((String) map.get("pageNum"));
            } else {
                throw new Exception("页数不正确");
            }
            if (map.containsKey("projectId") && StringUtils.isNotEmpty((String) map.get("projectId"))) {
                projects = (String) map.get("projectId");
            } else {
                throw new Exception("项目名不正确");
            }
        }
        return this;
    }
}
