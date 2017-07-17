package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_DOCK_IMAGES")
public class AdDockImages extends Model {
    @SequenceGenerator(name = "id_docimage_seq", sequenceName = "AD_DOCK_IMAGES$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_docimage_seq")
    @Id
    @Size(max = 12)
    Long dockImageId;
    String tag;
    Integer imageStatus;
    Date createDate;
    String projectName;
    String remark;
    Integer hasImage;

}
