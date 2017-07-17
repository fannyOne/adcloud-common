package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_PROJECT_CODE_REPORT")
public class AdProjectCodeReport extends Model {
    @Id
    Long codeReportId;
    Long projectId;
    Long codeNum;
    Long fileNum;
    Date createDate;

}
