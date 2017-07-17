package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by zhangpeng on 2016/7/21.
 */

@Data
@Entity
@Table(name = "AD_DOCKER_INFO")
public class AdDockerInfo extends Model {
    @Id
    String containersId;
    String imagesName;
    Integer branchId;
    Integer status;
    Date createDate;
    Date updateDate;
    String ext1;
}
