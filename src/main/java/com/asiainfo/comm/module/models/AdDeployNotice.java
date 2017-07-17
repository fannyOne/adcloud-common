package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Data
@Getter
@Setter
@Entity
@Table(name = "AD_DEPLOY_NOTICE")
public class AdDeployNotice extends Model {
    @Id
    @Size(max = 10)
    Long noticeId;
    String noticeContent;
    Timestamp createDate;

}
