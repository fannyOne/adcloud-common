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
@Table(name = "AD_BUILD_DEPLOY_DATA")
public class AdBuildDeployData extends Model {
    @SequenceGenerator(name = "id_seq", sequenceName = "ad_build_deploy_data_seq", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    Long id;
    String projectName;
    String brachName;
    Double bSuccessNum;
    Double bFailNum;
    Double bExecuTimes;
    Double dSuccessNum;
    Double dFailNum;
    Double dExecuTimes;
    Date createTime;
    Long ext1;
    String ext2;
    Long projectId;
}
