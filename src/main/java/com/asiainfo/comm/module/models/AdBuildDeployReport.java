package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by zhangpeng on 2016/7/12.
 */
@Data
@Entity
@Table(name = "AD_BUILD_DEPLOY_REPORT")
public class AdBuildDeployReport extends Model {
    @SequenceGenerator(name = "id_seq", sequenceName = "ad_build_deploy_report_seq", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    Long id;
    String projectName;
    String branchName;
    Double bSuccessRate;
    Double bAverageTimes;
    Double dSuccessRate;
    Double dAverageTimes;
    Date beginDate;
    Date endDate;
    Double type;
    Long ext1;
    Long ext2;
}
