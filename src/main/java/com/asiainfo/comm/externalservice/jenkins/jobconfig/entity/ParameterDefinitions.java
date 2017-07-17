package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;


@XStreamAlias("parameterDefinitions")
public class ParameterDefinitions {

    @XStreamImplicit(itemFieldName = "hudson.model.StringParameterDefinition")
    List<StringParameterDefinition> stringParams;

    public ParameterDefinitions() {
        stringParams = new ArrayList<StringParameterDefinition>();
    }

    public ParameterDefinitions(List<StringParameterDefinition> stringParams) {
        this.stringParams = stringParams;
    }

    public List<StringParameterDefinition> getStringParams() {
        return stringParams;
    }

    public void setStringParams(List<StringParameterDefinition> stringParams) {
        this.stringParams = stringParams;
    }

    public void addParam(StringParameterDefinition spd) {
        stringParams.add(spd);
    }
}
