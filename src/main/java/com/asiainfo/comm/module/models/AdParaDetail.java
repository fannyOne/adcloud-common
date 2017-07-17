package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "AD_PARA_DETAIL")
public class AdParaDetail extends Model {
    String regionId;
    String paraType;
    @Id
    String paraCode;
    String paraName;
    String para1;
    String para2;
    String para3;
    String para4;
    String para5;
    String paraDesc;
    String state;
    Long opId;
    Timestamp stateDate;
    String remarks;

    public String getParaCode() {
        return this.paraCode;
    }

    public String getPara1() {
        return this.para1;
    }

    public String getPara2() {
        return this.para2;
    }

    public void setPara2(String para2) {
        this.para2 = para2;
    }

    public String getPara3() {
        return this.para3;
    }

    public void setPara3(String para3) {
        this.para3 = para3;
    }

    public String getPara4() {
        return this.para4;
    }

    public void setPara4(String para4) {
        this.para4 = para4;
    }

    public String getPara5() {
        return this.para5;
    }

    public void setPara5(String para5) {
        this.para5 = para5;
    }

    public String getParaDesc() {
        return this.paraDesc;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getOpId() {
        return this.opId;
    }

    public void setOpId(Long opId) {
        this.opId = opId;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

}
