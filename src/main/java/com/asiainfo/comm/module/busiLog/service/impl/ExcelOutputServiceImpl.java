package com.asiainfo.comm.module.busiLog.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.ExcelCellExtPojo;
import com.asiainfo.comm.common.pojo.pojoExt.ExcelCellHeadExtPojo;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * Created by weif on 2016/11/9.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class ExcelOutputServiceImpl {

    public WritableWorkbook createExcelOutputExcel(HttpServletResponse response, HttpServletRequest request, List<ExcelCellHeadExtPojo> headList, List<ExcelCellExtPojo> contents, String fileName, List<ExcelCellHeadExtPojo> headList2) {
        fileName = fileName + ".xls";
        WritableWorkbook writableWorkbook = null;
        try {
            //addHeader增加头文件里没有的属性
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            //在浏览器中弹出窗口,给文件名编码,防止中文乱码,区分火狐浏览器和非火狐浏览器
            if (request.getHeader("USER-AGENT").toLowerCase().contains("firefox")) {
                //如果是火狐浏览器,则使用下面的方式为excel文件编码
                response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("GB2312"), "ISO-8859-1"));
            } else {
                response.setHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            }
            ServletOutputStream outputStream = response.getOutputStream();
            writableWorkbook = Workbook.createWorkbook(outputStream);
            WritableSheet excelOutputsheet = writableWorkbook.createSheet(fileName, 0);
            addExcelOutputHeader(headList, excelOutputsheet, 0);
            if (headList2 != null && headList2.size() > 0) {
                addExcelOutputHeader(headList2, excelOutputsheet, 1);
            }
            writeExcelOutputData(contents, excelOutputsheet, headList, headList2);
            writableWorkbook.write();
            writableWorkbook.close();
        } catch (Exception e) {
            log.error("Error occured while creating Excel file", e);
        }

        return writableWorkbook;
    }

    private void addExcelOutputHeader(List<ExcelCellHeadExtPojo> headList, WritableSheet sheet, int rownum) throws RowsExceededException, WriteException {
        // create header row
        int li_column = 0;
        int oldMerge;
        for (ExcelCellHeadExtPojo excelCellHeadExtPojo : headList) {
            sheet.addCell(new Label(li_column, rownum, excelCellHeadExtPojo.getCellValue()));
            if (excelCellHeadExtPojo.isMergeColumn()) {
                oldMerge = li_column;
                li_column += excelCellHeadExtPojo.getMergeColumnNum();
                sheet.mergeCells(oldMerge, rownum, li_column, rownum);
            }
            if (excelCellHeadExtPojo.isMergeRow()) {
                sheet.mergeCells(li_column, rownum, li_column, excelCellHeadExtPojo.getMergeRowNum());
            }
            li_column++;
        }
    }

    private void writeExcelOutputData(List<ExcelCellExtPojo> rows, WritableSheet sheet, List<ExcelCellHeadExtPojo> headList, List<ExcelCellHeadExtPojo> headList2) throws RowsExceededException, WriteException {
        int rowNo = 0;
        int columnNo = 0;
        if (headList != null && headList.size() > 0) {
            rowNo++;
        }
        if (headList2 != null && headList2.size() > 0) {
            rowNo++;
        }
        for (ExcelCellExtPojo excelCellExtPojo : rows) {
            if (excelCellExtPojo.getColumnList() != null) {
                columnNo = 0;
                for (String cellvalue : excelCellExtPojo.getColumnList()) {
                    sheet.addCell(new Label(columnNo, rowNo, cellvalue));
                    columnNo++;
                }
                rowNo++;
            }
        }
    }

}
