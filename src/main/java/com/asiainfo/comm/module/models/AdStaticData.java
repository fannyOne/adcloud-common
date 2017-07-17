package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "AD_STATIC_DATA")
public class AdStaticData extends Model {
    String codeType;
    String codeValue;
    String codeName;
    String codeDesc;
    String codeTypeAlias;
    Integer sortId;
    String state;
    String externCodeType;

    public String getCodeType() {
        return this.codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getCodeValue() {
        return this.codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }

    public String getCodeName() {
        return this.codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getCodeDesc() {
        return this.codeDesc;
    }

    public void setCodeDesc(String codeDesc) {
        this.codeDesc = codeDesc;
    }

    public String getCodeTypeAlias() {
        return this.codeTypeAlias;
    }

    public void setCodeTypeAlias(String codeTypeAlias) {
        this.codeTypeAlias = codeTypeAlias;
    }

    public void setSortId(Integer sortId) {
        this.sortId = sortId;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getExternCodeType() {
        return this.externCodeType;
    }

    public void setExternCodeType(String externCodeType) {
        this.externCodeType = externCodeType;
    }

}
