package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_SONAR_DATA")
public class AdSonarData extends Model {
    @SequenceGenerator(name = "id_seq", sequenceName = "ad_sonar_data_seq", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    Long id;
    //int long boolean char float short double
    String projectName;
    Long filenums;
    Long filenumsC;
    Long methodnums;
    ;
    Long methodnumsC;
    Long codelines;
    Long codelinesC;
    Double repeat;
    Double repeatC;
    Double methodComp;
    Double methodCompC;
    Long dComplexity;
    Long dComplexityC;
    Long seriousIssues;
    Long seriousIssuesC;
    Long blockIssues;
    Long blockIssuesC;
    Double coverage;
    Double coverageC;
    Double unitSuccessRate;
    Double unitSuccessRateC;
    Long unitnums;
    Long unitnumsC;
    Double unittime;
    Double unittimeC;
    Date scanDate;
    Integer state;
}
