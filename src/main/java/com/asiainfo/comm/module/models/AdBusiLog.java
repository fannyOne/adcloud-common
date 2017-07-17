package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_BUSI_LOG")
public class AdBusiLog extends Model {
    @Id
    @SequenceGenerator(name = "ad_busi_log_id_seq", sequenceName = "AD_BUSI_LOG$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ad_busi_log_id_seq")
    Long busiLogId;
    Long projectId;
    Long opId;
    String opName;
    Long busiCode;
    String busiDetail;
    Long doneCode;
    @Temporal(TemporalType.DATE)
    Date createDate;

}
