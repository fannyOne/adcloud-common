package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.AdEasyUIPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.ExcelCellExtPojo;
import com.asiainfo.comm.common.pojo.pojoExt.ExcelCellHeadExtPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.AdEasyUIPojo;
import com.asiainfo.comm.module.build.dao.impl.AdSonarReportDAO;
import com.asiainfo.comm.module.build.dao.impl.AdTreeDataDAO;
import com.asiainfo.comm.module.models.AdSonarData;
import com.asiainfo.comm.module.models.AdSonarReport;
import com.asiainfo.comm.module.models.AdTreeData;
import com.avaje.ebean.SqlRow;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangpeng on 2016/7/6.
 */
@Component
public class AdSonarReportImpl {
    @Autowired
    AdSonarReportDAO adSonarReportDAO;
    @Autowired
    AdTreeDataDAO treeDataDAO;

    public List<AdSonarReport> qryById(Long id) {
        return adSonarReportDAO.qryById(id);
    }

    public void getAdSonarReportByDateAndType(String name, long reports) {
        adSonarReportDAO.getAdSonarReportByDateAndType(name, reports);
    }

    public void delBySonarReptId(Long id) {
        adSonarReportDAO.delBySonarReptId(id);
    }

    public List<SqlRow> getAdSonarReportByNameAndDate(String project_name, Long type, String date) {
        return adSonarReportDAO.getAdSonarReportByNameAndDate(project_name, type, date);
    }

    public List<AdSonarData> obtSonarDataSonarId(Long sonarId) {
        return adSonarReportDAO.obtSonarDataSonarId(sonarId);
    }

    public List<SqlRow> getAdReportByNameAndDate(String project_name, Long type, String date) {
        return adSonarReportDAO.getAdReportByNameAndDate(project_name, type, date);
    }

    public AdEasyUIPojo qrySonarDateReport(String groupIds, String filters, String startDate, String endDate) {
        DecimalFormat df = new DecimalFormat("#0.0");
        AdEasyUIPojo poj = new AdEasyUIPojo();
        AdEasyUIPojoExt ext = new AdEasyUIPojoExt();
        List<AdTreeData> treeDataList = treeDataDAO.qryByIds(filters);
        @Data
        class TempFilter {
            private String filter;
            private Integer code;
            private Integer normal;
        }
        String filter;
        List<TempFilter> tempFilterList = new ArrayList<>();
        if (treeDataList != null) {
            for (AdTreeData data : treeDataList) {
                TempFilter tempFilter = new TempFilter();
                tempFilter.setFilter(data.getTreeCode());
                tempFilter.setCode(Integer.parseInt(data.getTreePara()));
                tempFilter.setNormal(Integer.parseInt(data.getTreeDesc()));
                tempFilterList.add(tempFilter);
            }
        }
        if (endDate == null) {
            List<SqlRow> sonarData = adSonarReportDAO.qrySonarDate(startDate, groupIds);
            ext.setTotal(sonarData.size());
            List<Map<String, Object>> rows = new ArrayList<>();
            for (SqlRow sqlRow : sonarData) {
                Map<String, Object> row = new HashMap<>();
                row.put("projectId", sqlRow.get("GROUP_NAME"));
                for (TempFilter tempFilter : tempFilterList) {
                    filter = tempFilter.getFilter();
                    switch (tempFilter.getCode()) {
                        case 1://整数
                            row.put(filter, sqlRow.getLong(filter));
                            break;
                        case 2://浮点数
                            row.put(filter, df.format(sqlRow.getDouble(filter)));
                            break;
                        case 3://百分比
                            row.put(filter, sqlRow.getString(filter) + "%");
                            break;
                        case 4://时间
                            row.put(filter, sqlRow.getString(filter) + "s");
                            break;
                        default:
                            break;
                    }
                }
                rows.add(row);
            }
            ext.setRows(rows);
        } else {
            boolean normal;
            List<SqlRow> nowSonarData = adSonarReportDAO.qrySonarDate(startDate, groupIds);
            List<SqlRow> vsSonarData = adSonarReportDAO.qrySonarDateFilter(startDate, endDate, groupIds);
            Map<Object, SqlRow> vsSonarDataMap = new HashMap<>();
            if (vsSonarData.size() > 0) {
                for (SqlRow sqlRow : vsSonarData) {
                    vsSonarDataMap.put(sqlRow.get("GROUP_NAME"), sqlRow);
                }
            }
            ext.setTotal(nowSonarData.size());
            List<Map<String, Object>> rows = new ArrayList<>();
            String[] dou;
            for (SqlRow sqlRow : nowSonarData) {
                Map<String, Object> row = new HashMap<>();
                row.put("projectId", sqlRow.get("GROUP_NAME"));
                int intNum;
                double douNum;
                if (vsSonarDataMap.containsKey(sqlRow.get("GROUP_NAME"))) {
                    for (TempFilter tempFilter : tempFilterList) {
                        dou = new String[3];
                        filter = tempFilter.getFilter();
                        normal = tempFilter.getNormal() == 1;
                        switch (tempFilter.getCode()) {
                            case 1://整数
                                intNum = (sqlRow.getInteger(filter) - vsSonarDataMap.get(sqlRow.get("GROUP_NAME")).getInteger(filter));
                                dou[0] = sqlRow.getInteger(filter) + "";
                                dou[1] = "" + intNum;
                                dou[2] = (normal ? (intNum < 0) : (intNum >= 0)) + "";
                                row.put(filter, dou);
                                break;
                            case 2://浮点数
                                douNum = (sqlRow.getDouble(filter) - vsSonarDataMap.get(sqlRow.get("GROUP_NAME")).getDouble(filter));
                                dou[0] = df.format(sqlRow.getDouble(filter)) + "";
                                dou[1] = "" + df.format(douNum);
                                dou[2] = (normal ? (douNum < 0) : (douNum >= 0)) + "";
                                row.put(filter, dou);
                                break;
                            case 3://百分比
                                douNum = (sqlRow.getDouble(filter) - vsSonarDataMap.get(sqlRow.get("GROUP_NAME")).getDouble(filter));
                                dou[0] = df.format(sqlRow.getDouble(filter)) + "%";
                                dou[1] = "" + df.format(douNum);
                                dou[2] = (normal ? (douNum < 0) : (douNum >= 0)) + "";
                                row.put(filter, dou);
                                break;
                            case 4://时间
                                intNum = (sqlRow.getInteger(filter) - vsSonarDataMap.get(sqlRow.get("GROUP_NAME")).getInteger(filter));
                                dou[0] = sqlRow.getInteger(filter) + "s";
                                dou[1] = "" + intNum;
                                dou[2] = (normal ? (intNum < 0) : (intNum >= 0)) + "";
                                row.put(filter, dou);
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    for (TempFilter tempFilter : tempFilterList) {
                        dou = new String[3];
                        filter = tempFilter.getFilter();
                        normal = tempFilter.getNormal() == 1;
                        switch (tempFilter.getCode()) {
                            case 1://整数
                                intNum = 0;
                                dou[0] = sqlRow.getInteger(filter) + "";
                                dou[1] = "" + intNum;
                                dou[2] = (!normal) + "";
                                row.put(filter, dou);
                                break;
                            case 2://浮点数
                                douNum = 0.0;
                                dou[0] = df.format(sqlRow.getDouble(filter)) + "";
                                dou[1] = "" + df.format(douNum);
                                dou[2] = (!normal) + "";
                                row.put(filter, dou);
                                break;
                            case 3://百分比
                                douNum = 0.0;
                                dou[0] = df.format(sqlRow.getDouble(filter)) + "%";
                                dou[1] = df.format(douNum);
                                dou[2] = (!normal) + "";
                                row.put(filter, dou);
                                break;
                            case 4://时间
                                intNum = 0;
                                dou[0] = sqlRow.getInteger(filter) + "s";
                                dou[1] = "" + intNum;
                                dou[2] = (!normal) + "";
                                row.put(filter, dou);
                                break;
                            default:
                                break;
                        }
                    }
                }
                rows.add(row);
            }
            ext.setRows(rows);
        }
        poj.setEasyUIPojoExt(ext);
        return poj;
    }

    public void setSonarReportHead(List<ExcelCellHeadExtPojo> headList, List<ExcelCellHeadExtPojo> headExtList, String filters) {
        long headOld = 0L;
        int headNum = 0;
        List<SqlRow> sqlRowList;
        sqlRowList = treeDataDAO.qryTreeDataByIdList(filters);
        if (sqlRowList == null || sqlRowList.size() <= 0) {
            return;
        }
        {
            ExcelCellHeadExtPojo head = new ExcelCellHeadExtPojo();
            ExcelCellHeadExtPojo headExt = new ExcelCellHeadExtPojo();
            head.setCellValue("项目名称");
            head.setMergeRow(true);
            headExt.setCellValue("");
            headList.add(head);
            headExtList.add(headExt);
        }
        for (int i = 0; i < sqlRowList.size(); i++) {
            if (i == sqlRowList.size() - 1) {
                ExcelCellHeadExtPojo head = new ExcelCellHeadExtPojo();
                head.setCellValue(sqlRowList.get(i).getString("P_TREE_NAME"));
                head.setMergeColumn(true);
                head.setMergeColumnNum(headNum);
                headList.add(head);
            } else if (i == 0) {
                headOld = sqlRowList.get(i).getLong("P_ID");
            } else if (sqlRowList.get(i).getLong("P_ID") != headOld) {
                ExcelCellHeadExtPojo head = new ExcelCellHeadExtPojo();
                head.setCellValue(sqlRowList.get(i - 1).getString("P_TREE_NAME"));
                head.setMergeColumn(true);
                head.setMergeColumnNum(headNum - 1);
                headList.add(head);
                headNum = 0;
                headOld = sqlRowList.get(i).getLong("P_ID");
            }
            ExcelCellHeadExtPojo headExt = new ExcelCellHeadExtPojo();
            headExt.setCellValue(sqlRowList.get(i).getString("TREE_NAME"));
            headExtList.add(headExt);
            headNum++;
        }
    }

    public void setSonarReportContent(List<ExcelCellExtPojo> contents, String groupIds, String filters, String startDate, String endDate) {
        DecimalFormat df = new DecimalFormat("#0.0");//小数格式，保留小数点后一位
        List<AdTreeData> treeDataList = treeDataDAO.qryByIds(filters);
        //属性内部类，对应的编码和类型编号
        @Data
        class TempFilter {
            private String filter;
            private Integer code;
            private Integer normal;
        }
        List<TempFilter> tempFilterList = new ArrayList<>();
        String filter;
        if (treeDataList != null) {
            for (AdTreeData data : treeDataList) {
                TempFilter tempFilter = new TempFilter();
                tempFilter.setFilter(data.getTreeCode());
                tempFilter.setCode(Integer.parseInt(data.getTreePara()));
                tempFilter.setNormal(Integer.parseInt(data.getTreeDesc()));
                tempFilterList.add(tempFilter);
            }
        }
        if (endDate == null) {
            List<SqlRow> sonarData = adSonarReportDAO.qrySonarDate(startDate, groupIds);
            for (SqlRow sqlRow : sonarData) {
                ExcelCellExtPojo content = new ExcelCellExtPojo();
                List<String> columnList = new ArrayList<>();
                columnList.add(sqlRow.getString("GROUP_NAME"));
                for (TempFilter tempFilter : tempFilterList) {
                    filter = tempFilter.getFilter();
                    String data = switchSonarData(sqlRow, filter, tempFilter.getCode(), df);//数据拼装
                    columnList.add(data);
                }
                content.setColumnList(columnList);
                contents.add(content);
            }
        } else {
            List<SqlRow> nowSonarData = adSonarReportDAO.qrySonarDate(startDate, groupIds);
            List<SqlRow> vsSonarData = adSonarReportDAO.qrySonarDateFilter(startDate, endDate, groupIds);
            Map<Object, SqlRow> vsSonarDataMap = new HashMap<>();
            if (vsSonarData.size() > 0) {
                for (SqlRow sqlRow : vsSonarData) {
                    vsSonarDataMap.put(sqlRow.get("GROUP_NAME"), sqlRow);
                }
            }
            for (SqlRow sqlRow : nowSonarData) {
                Map<String, Object> row = new HashMap<>();
                row.put("projectId", sqlRow.get("GROUP_NAME"));
                if (vsSonarDataMap.containsKey(sqlRow.get("GROUP_NAME"))) {
                    ExcelCellExtPojo content = new ExcelCellExtPojo();
                    List<String> columnList = new ArrayList<>();
                    columnList.add(sqlRow.getString("GROUP_NAME"));
                    for (TempFilter tempFilter : tempFilterList) {
                        filter = tempFilter.getFilter();
                        String data = switchSonarDataCompare(sqlRow, vsSonarDataMap.get(sqlRow.get("GROUP_NAME")), filter, tempFilter.getCode(), df);//数据拼装
                        columnList.add(data);
                    }
                    content.setColumnList(columnList);
                    contents.add(content);
                } else {
                    ExcelCellExtPojo content = new ExcelCellExtPojo();
                    List<String> columnList = new ArrayList<>();
                    columnList.add(sqlRow.getString("GROUP_NAME"));
                    for (TempFilter tempFilter : tempFilterList) {
                        filter = tempFilter.getFilter();
                        String data = switchSonarDataCompare(sqlRow, null, filter, tempFilter.getCode(), df);//数据拼装
                        columnList.add(data);
                    }
                    content.setColumnList(columnList);
                    contents.add(content);
                }
            }
        }
    }

    public String switchSonarDataCompare(SqlRow sqlRow, SqlRow compareSqlRow, String filter, Integer code, DecimalFormat df) {
        String data;
        if (compareSqlRow == null) {
            switch (code) {
                case 1://整数
                    data = sqlRow.getInteger(filter) + "\n"
                        + compareNumberInt(sqlRow.getInteger(filter), sqlRow.getInteger(filter));
                    break;
                case 2://浮点数
                    data = df.format(sqlRow.getDouble(filter)) + "\n"
                        + compareNumberDouble(sqlRow.getDouble(filter), sqlRow.getDouble(filter), df);
                    break;
                case 3://百分比
                    data = df.format(sqlRow.getDouble(filter)) + "%\n"
                        + compareNumberDouble(sqlRow.getDouble(filter), sqlRow.getDouble(filter), df);
                    break;
                case 4://时间
                    data = sqlRow.getInteger(filter) + "s\n"
                        + compareNumberInt(sqlRow.getInteger(filter), sqlRow.getInteger(filter));
                    break;
                default:
                    data = "";
                    break;
            }
        } else {
            switch (code) {
                case 1://整数
                    data = sqlRow.getInteger(filter) + "\n"
                        + compareNumberInt(sqlRow.getInteger(filter), compareSqlRow.getInteger(filter));
                    break;
                case 2://浮点数
                    data = df.format(sqlRow.getDouble(filter)) + "\n"
                        + compareNumberDouble(sqlRow.getDouble(filter), compareSqlRow.getDouble(filter), df);
                    break;
                case 3://百分比
                    data = df.format(sqlRow.getDouble(filter)) + "%\n"
                        + compareNumberDouble(sqlRow.getDouble(filter), compareSqlRow.getDouble(filter), df);
                    break;
                case 4://时间
                    data = sqlRow.getInteger(filter) + "s\n"
                        + compareNumberInt(sqlRow.getInteger(filter), compareSqlRow.getInteger(filter));
                    break;
                default:
                    data = "";
                    break;
            }
        }
        return data;
    }

    public String switchSonarData(SqlRow sqlRow, String filter, Integer code, DecimalFormat df) {
        String data;
        switch (code) {
            case 1://整数
                data = sqlRow.getLong(filter) + "";
                break;
            case 2://浮点数
                data = df.format(sqlRow.getDouble(filter));
                break;
            case 3://百分比
                data = sqlRow.getString(filter) + "%";
                break;
            case 4://时间
                data = sqlRow.getString(filter) + "s";
                break;
            default:
                data = "";
                break;
        }
        return data;
    }

    public String compareNumberInt(int data1, int data2) {
        int dataCompare = data1 - data2;
        if (dataCompare > 0) {
            return "(" + dataCompare + "↑)";
        } else if (dataCompare < 0) {
            return "(" + (-dataCompare) + "↓)";
        } else {
            return "";
        }
    }

    public String compareNumberDouble(double data1, double data2, DecimalFormat df) {
        double dataCompareDouble = data1 - data2;
        if (dataCompareDouble > 0) {
            return "(" + df.format(dataCompareDouble) + "↑)";
        } else if (dataCompareDouble < 0) {
            return "(" + df.format(-dataCompareDouble) + "↓)";
        } else {
            return "";
        }
    }
}
