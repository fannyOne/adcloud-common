package com.asiainfo.comm.module.report.controller;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.ExcelCellExtPojo;
import com.asiainfo.comm.common.pojo.pojoExt.ExcelCellHeadExtPojo;
import com.asiainfo.comm.common.pojo.pojoExt.OperationGroupReportPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.GroupReportPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.OperationGroupReportPojo;
import com.asiainfo.comm.module.busiLog.service.impl.ExcelOutputServiceImpl;
import com.asiainfo.comm.module.report.service.impl.OperationReportImpl;
import com.asiainfo.comm.module.role.controller.BaseController;
import com.google.common.collect.Lists;
import jxl.write.WritableWorkbook;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/operationReport")
public class OperationReportController extends BaseController {
    @Autowired
    OperationReportImpl operationReportImpl;
    @Autowired
    ExcelOutputServiceImpl excelOutputService;

    @RequestMapping(value = "/operationCollect", produces = "application/json")
    public Pojo operationCollect() {
        return operationReportImpl.collectInfo();
    }

    @RequestMapping(value = "/operationGroup", produces = "application/json")
    public Pojo operationGroup(HttpServletRequest request, String groupIds, String qryType) {
        OperationGroupReportPojo pojo = new OperationGroupReportPojo();
        pojo.setQryData(operationReportImpl.qryGroupReport(getUserId(request), groupIds, qryType));
        return pojo;
    }


    @RequestMapping(value = "/outputExcel")
    public WritableWorkbook outputExcel(HttpServletResponse response, HttpServletRequest request, String groupIds, String qryType) throws IOException {
        String fileName = "ADCloud接入报表";
        List<ExcelCellHeadExtPojo> headList = buildHeadListByType(qryType);
        OperationGroupReportPojoExt pojo = operationReportImpl.qryGroupReport(getUserId(request), groupIds, qryType);
        List<GroupReportPojo> pojos = pojo.getRows();
        List<ExcelCellExtPojo> contents = buildBodyListByType(pojos, qryType);
        return excelOutputService.createExcelOutputExcel(response, request, headList, contents, fileName, null);
    }

    private List<ExcelCellHeadExtPojo> buildHeadListByType(String qryType) {
        List<ExcelCellHeadExtPojo> headList = Lists.newArrayList();
        if (StringUtils.isNotEmpty(qryType)) {
            ExcelCellHeadExtPojo pojo = new ExcelCellHeadExtPojo();
            pojo.setCellValue("项目名称");
            headList.add(pojo);
            for (String head : qryType.split(",")) {
                ExcelCellHeadExtPojo date = new ExcelCellHeadExtPojo();
                int itype = Integer.parseInt(head);
                String value = "";
                switch (itype) {
                    case 26:// 使用次数
                        value = "使用次数";
                        break;
                    case 27://  接入环境
                        value = "接入环境";
                        break;
                    case 28://  接入流水
                        value = "接入流水";
                        break;
                    case 29://平均构建时长
                        value = "平均构建时长";
                        break;
                    case 30://用户数
                        value = "用户数";
                        break;
                    default:
                        break;
                }
                date.setCellValue(value);
                headList.add(date);
            }
        }
        return headList;
    }

    private List<ExcelCellHeadExtPojo> buildHeadList() {
        String heads = "项目名称,使用次数,接入环境,接入流水,平均构建时长,用户数";
        List<ExcelCellHeadExtPojo> headList = Lists.newArrayList();
        for (String head : heads.split(",")) {
            ExcelCellHeadExtPojo pojo = new ExcelCellHeadExtPojo();
            pojo.setCellValue(head);
            headList.add(pojo);
        }
        return headList;
    }

    private List<ExcelCellExtPojo> buildBodyListByType(List<GroupReportPojo> reprots, String qryType) {
        List<ExcelCellExtPojo> pojos = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(reprots)) {
            for (GroupReportPojo report : reprots) {
                ExcelCellExtPojo pojo = new ExcelCellExtPojo();
                List<String> strings = Lists.newArrayList();
                strings.add(report.getGroupName());
                if (qryType.contains("26"))
                    strings.add(String.valueOf(report.getUsedCount()));
                if (qryType.contains("27"))
                    strings.add(String.valueOf(report.getEnvCount()));
                if (qryType.contains("28"))
                    strings.add(String.valueOf(report.getBranchCount()));
                if (qryType.contains("29"))
                    strings.add(String.valueOf(report.getAvgTime()));
                if (qryType.contains("30"))
                    strings.add(String.valueOf(report.getGroupUserCount()));
                pojo.setColumnList(strings);
                pojos.add(pojo);
            }
        }
        return pojos;
    }

}
