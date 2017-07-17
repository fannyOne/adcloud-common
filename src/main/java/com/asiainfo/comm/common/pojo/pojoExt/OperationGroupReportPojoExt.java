package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.pojoMaster.GroupReportPojo;
import lombok.Data;

import java.util.List;

/**
 * Created by zhenghp on 2016/11/9.
 */
@Data
public class OperationGroupReportPojoExt {
    List<GroupReportPojo> rows;
    Long total;
}
