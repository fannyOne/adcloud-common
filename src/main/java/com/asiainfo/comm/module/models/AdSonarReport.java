package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by zhangpeng on 2016/7/6.
 */

@Data
@Entity
@Table(name = "AD_SONAR_REPORT")
public class AdSonarReport extends Model {
    @SequenceGenerator(name = "id_seq", sequenceName = "ad_sonar_report_sqe", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "SONAR_ID", nullable = false, updatable = false, referencedColumnName = "Id")
    AdSonarData adSonarData;
    //int long boolean char float short double
    String projectName;
    Long filenumsC;
    Long methodnumsC;
    Long codelinesC;
    Double repeatC;
    Double methodCompC;
    Long dComplexityC;
    Long seriousIssuesC;
    Long blockIssuesC;
    Double coverageC;
    Double unitSuccessRateC;
    Long unitnumsC;
    Double unittimeC;
    Date scanDate;
    Long reports;
    String ext;
}
