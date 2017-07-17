package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "AD_DCOS_DEPLOY_DTL")
public class AdDcosDeployDtl extends Model {

    Long branchId;
    Long deployInfoId;
    String packageName;
    String appid;
    Integer priorityNum;
    Integer state;
}
