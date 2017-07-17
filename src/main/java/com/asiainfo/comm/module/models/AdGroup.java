package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Data
@Table(name = "AD_GROUP")
public class AdGroup extends Model {

    @SequenceGenerator(name = "id_seq", sequenceName = "AD_GROUP$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    Long groupId;

    String groupName;

    Integer groupType;

    String groupDesc;

    @Column(columnDefinition = "Date")
    Date createDate;

    @Column(columnDefinition = "Date")
    Date doneDate;

    @Column(columnDefinition = "Date")
    Date expireDate;

    Integer state;

/*    @OneToMany
//    @JoinColumn(name = "PROJECT_ID", updatable = false)
    List<AdProject> adProjects;*/

    String opUser;

    String groupIdExt;
}
