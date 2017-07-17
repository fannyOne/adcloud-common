package com.asiainfo.comm.module.report.controller;

import com.asiainfo.comm.common.pojo.pojoExt.ExcelCellExtPojo;
import com.asiainfo.comm.common.pojo.pojoExt.ExcelCellHeadExtPojo;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.JsonUtil;
import com.asiainfo.comm.module.build.service.impl.AdBuildDeployDataImpl;
import com.asiainfo.comm.module.build.service.impl.AdBuildLogImpl;
import com.asiainfo.comm.module.build.service.impl.AdUserDataRelateImpl;
import com.asiainfo.comm.module.busiLog.service.impl.ExcelOutputServiceImpl;
import com.asiainfo.comm.module.role.controller.BaseController;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weif on 2016/11/10.
 */
@RestController
@lombok.extern.slf4j.Slf4j
@RequestMapping(value = "/BuildLogReport")
public class BuildLogReportController extends BaseController {

    @Autowired
    AdBuildDeployDataImpl adBuildDeployData;
    @Autowired
    AdUserDataRelateImpl adUserDataRelate;
    @Autowired
    ExcelOutputServiceImpl excelOutputService;
    @Autowired
    private AdBuildLogImpl adBuildLog;

    @RequestMapping(value = "/LogReport", method = RequestMethod.POST)
    public String stateBuildLog(@RequestParam Map<String, Object> param, HttpServletRequest request) throws Exception {
        String beginDate;
        String endDate;
        int qryType;
        String groupIds;
        Map retmap = new HashMap<>();
        if (param.get("beginDate") != null) {
            beginDate = (String) param.get("beginDate");
        } else {
            throw new Exception("请选择开始时间");
        }
        if (param.get("endDate") != null) {
            endDate = (String) param.get("endDate");
        } else {
            throw new Exception("请选择结束时间");
        }
        if (param.get("qryType") != null) {
            qryType = Integer.parseInt((String) param.get("qryType"));
        } else {
            throw new Exception("请选择环节");
        }
        if (param.get("groupIds") != null) {
            groupIds = (String) param.get("groupIds");
            if (groupIds.indexOf(",") >= 0 && (",").equals(groupIds.substring(groupIds.length() - 1, groupIds.length()))) {
                groupIds = groupIds.substring(0, groupIds.length() - 1);
            }
        } else {
            throw new Exception("请选择项目");
        }
        // 保存勾选的group
        adUserDataRelate.updateRel(getUserId(request), 2, 2, groupIds);
        List<Map<String, String>> buildList = adBuildDeployData.qryBuildLogReport(beginDate, endDate, groupIds, qryType);
        retmap.put("rows", buildList);
        int total = 0;
        if (buildList != null) {
            total = buildList.size();
        }
        retmap.put("total", total);
        String ret = JsonUtil.mapToJson(retmap);
        return ret;
    }

    @RequestMapping(value = "/LogReportV2", method = RequestMethod.POST)
    public String stateBuildLogV2(@RequestParam Map<String, Object> param, HttpServletRequest request) throws Exception {
        String beginDate;
        String endDate;
        int qryType;
        String groupIds;
        Map retmap = new HashMap<>();
        if (param.get("beginDate") != null) {
            beginDate = (String) param.get("beginDate");
        } else {
            throw new Exception("请选择开始时间");
        }
        if (param.get("endDate") != null) {
            endDate = (String) param.get("endDate");
        } else {
            throw new Exception("请选择结束时间");
        }
        if (param.get("qryType") != null) {
            qryType = Integer.parseInt((String) param.get("qryType"));
        } else {
            throw new Exception("请选择环节");
        }
        if (param.get("groupIds") != null) {
            groupIds = (String) param.get("groupIds");
            if (groupIds.indexOf(",") >= 0 && (",").equals(groupIds.substring(groupIds.length() - 1, groupIds.length()))) {
                groupIds = groupIds.substring(0, groupIds.length() - 1);
            }
        } else {
            throw new Exception("请选择项目");
        }
        // 保存勾选的group
        adUserDataRelate.updateRel(getUserId(request), 2, 2, groupIds);
        List<Map<String, Object>> buildList = adBuildDeployData.qryBuildLogReportV2(beginDate, endDate, groupIds, qryType);
        retmap.put("rows", buildList);
        int total = 0;
        if (buildList != null) {
            total = buildList.size();
        }
        retmap.put("total", total);
        String ret = JsonUtil.mapToJson(retmap);
        return ret;
    }

    @RequestMapping(value = "/stageReport", produces = "application/json")
    public String stageReport(@RequestParam Map map) throws Exception {
        long begin_time = System.currentTimeMillis();
        Map hmap = adBuildLog.qryStageReport(map);
        String ret = JsonUtil.mapToJson(hmap);
        long end_time = System.currentTimeMillis();
        System.out.print("***************" + (end_time - begin_time));
        return ret;
    }

    @RequestMapping(value = "/stageReportdtl", produces = "application/json")
    public String stageReportdtl(@RequestParam Map map) throws Exception {
        long begin_time = System.currentTimeMillis();
        Map hmap = adBuildLog.qryStageReportdtl(map);
        String ret = JsonUtil.mapToJson(hmap);
        long end_time = System.currentTimeMillis();
        System.out.print("***************" + (end_time - begin_time));
        return ret;
    }

    @RequestMapping(value = "/LogReportExport", method = RequestMethod.POST)
    public String BuildLogExport(@RequestParam Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String beginDate = null;
        String endDate = null;
        int qryType = 1;
        String groupIds = null;
        if (param.get("beginDate") != null) {
            beginDate = (String) param.get("beginDate");
        } else {
            throw new Exception("请选择开始时间");
        }
        if (param.get("endDate") != null) {
            endDate = (String) param.get("endDate");
        } else {
            throw new Exception("请选择结束时间");
        }
        if (param.get("qryType") != null) {
            qryType = Integer.parseInt((String) param.get("qryType"));
        } else {
            throw new Exception("请选择环节");
        }
        if (param.get("groupIds") != null) {
            groupIds = (String) param.get("groupIds");
            if (groupIds.contains(",") && (",").equals(groupIds.substring(groupIds.length() - 1, groupIds.length()))) {
                groupIds = groupIds.substring(0, groupIds.length() - 1);
            }
        } else {
            throw new Exception("请选择项目");
        }
        List<Map<String, String>> buildList = adBuildDeployData.qryBuildLogReport(beginDate, endDate, groupIds, qryType);
        List<ExcelCellHeadExtPojo> headlist = new ArrayList<ExcelCellHeadExtPojo>();
        List<ExcelCellHeadExtPojo> head_extlist = new ArrayList<ExcelCellHeadExtPojo>();
        List<ExcelCellExtPojo> contentsList = new ArrayList<ExcelCellExtPojo>();
        List<String> columnList;
        if (buildList != null) {
            int i = 0;
            ExcelCellHeadExtPojo excelCellHeadExtPojo;
            ExcelCellHeadExtPojo excelCellHeadExtPojo_ext;
            ExcelCellExtPojo excelCellExtPojo;
            String head_key = null;
            String oldhead_key = null;
            boolean flag = true;
            for (Map<String, String> hmap : buildList) {
                i++;
                excelCellExtPojo = new ExcelCellExtPojo();
                columnList = new ArrayList<String>();
                for (Map.Entry<String, String> entry : hmap.entrySet()) {
                    if (i <= 1) {
                        flag = true;
                        excelCellHeadExtPojo = new ExcelCellHeadExtPojo();
                        excelCellHeadExtPojo_ext = new ExcelCellHeadExtPojo();
                        excelCellExtPojo = new ExcelCellExtPojo();
                        head_key = entry.getKey();
                        if (CommConstants.BUILD_LOG_EXPORT_COLUMN.EXPORT_COLUMN.containsKey(head_key)) {
                            excelCellHeadExtPojo.setCellValue(CommConstants.BUILD_LOG_EXPORT_COLUMN.EXPORT_COLUMN.get(head_key));
                            excelCellHeadExtPojo.setMergeRow(true);
                            excelCellHeadExtPojo_ext.setCellValue("");
                        } else {
                            if (StringUtils.isNotEmpty(head_key)) {
                                if (head_key.indexOf("-true") >= 0) {
                                    excelCellHeadExtPojo_ext.setCellValue("成功");
                                } else if (head_key.indexOf("-false") >= 0) {
                                    excelCellHeadExtPojo_ext.setCellValue("失败");
                                }
                                head_key = head_key.replace("-true", "").replace("-false", "");
                                excelCellHeadExtPojo.setCellValue(head_key);
                                if (!head_key.equals(oldhead_key)) {
                                    excelCellHeadExtPojo.setMergeColumn(true);
                                } else {
                                    flag = false;
                                }
                            }
                        }
                        oldhead_key = head_key;
                        if (flag) {
                            headlist.add(excelCellHeadExtPojo);
                        }
                        head_extlist.add(excelCellHeadExtPojo_ext);
                    }
                    columnList.add(entry.getValue());
                    excelCellExtPojo.setColumnList(columnList);
                }
                contentsList.add(excelCellExtPojo);
            }
        }
        if (!headlist.isEmpty() && !contentsList.isEmpty()) {
            excelOutputService.createExcelOutputExcel(response, request, headlist, contentsList, "构建部署报表", head_extlist);
        }
        return "success";
    }


}
