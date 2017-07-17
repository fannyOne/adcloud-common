package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by liusteven on 17/6/2.
 */
@Data
@Entity
@Table(name = "AD_TIMED_BUILD_TASK_LOG")
public class AdTimedBuildTaskLog extends Model {

    @Id
    @SequenceGenerator(name = "AD_TIMED_BUILD_TASK_LOG_SEQ", sequenceName = "AD_TIMED_BUILD_TASK_LOG_SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_TIMED_BUILD_TASK_LOG_SEQ")
    Long taskLogId;
    Long branchId;
    Date time;
    Integer state;
    String result;
    String detail;
}
