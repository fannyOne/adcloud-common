package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Created by guojian on 9/28/16.
 */
@Table(name = "AD_VIRTUAL_ENVIRONMENT")
@Entity
@Data
public class AdVirtualEnvironment extends Model {
    @SequenceGenerator(name = "id_seq", sequenceName = "AD_VIRTUAL_ENVIRONMENT$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    Long virtualId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false, updatable = false)
    AdProject adProject;

    String virtualName;

    String serverUsername;
    String serverPassword;
    String serverUrl;

    String filePath;
    String fileName;
    Long env_type;
    Long state;
    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", nullable = false, updatable = false)
    AdBranch adBranch;
    String packageName;
    String sourceAddress;
    String destinationAddress;
    String restartShell;

    Integer region;     //所属域，1代表生产域，2代表测试域
}
