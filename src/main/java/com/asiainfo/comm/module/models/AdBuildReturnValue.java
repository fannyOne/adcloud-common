package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;

/**
 * Created by weif on 2016/7/11.
 */
@Data
@Entity
@Table(name = "AD_BUILD_RETURN_VALUE")
public class AdBuildReturnValue extends Model {
    @SequenceGenerator(name = "value_seq", sequenceName = "AD_BUILD_RETURN_VALUE$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "value_seq")
    @Id
    Long returnValueId;

    Long pipelineId;

    Long step;

    Long buildSeq;

    Long next_step;


}
