package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_JENKINS_INFO")
public class AdJenkinsInfo extends Model {
    @Id
    Long jenkinsId;
    String jenkinsUrl;
    String jenkinsUsername;
    String jenkinsPassword;
    Date createDate;
    Date doneDate;
    Date expireDate;
    Long state;
    String remarks;
    Integer serverPort;
    String serverUsername;
    String serverPassword;
    String pathShell;
    String jenkinsMode;

//    @ManyToOne(optional = false)
//    @JoinColumn(name = "OP_ID", nullable = false, updatable = false,referencedColumnName = "USER_ID")
//    AdUser adUser;

}
