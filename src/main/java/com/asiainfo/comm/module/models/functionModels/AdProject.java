package com.asiainfo.comm.module.models.functionModels;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_PROJECT")
public class AdProject extends Model {
    public static final Finder<Long, AdProject> finder = new Finder<Long, AdProject>(Long.class, AdProject.class);
    @Id
    @Size(max = 12)
    @SequenceGenerator(name = "id_project_seq", sequenceName = "AD_PROJECT$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_project_seq")
    Long projectId;

    String projectName;

    String reqSysType;

    Date createDate;

    Date doneDate;

    Date expireDate;

    Long state;

    String codeStore;

    String gitProjectid;

    //发布的应用id
    String deployAppId;

    //二进制包保存的知识库名字
    String deployRepository;
}
