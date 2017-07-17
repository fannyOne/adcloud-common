package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("hudson.model.ParametersDefinitionProperty")
public class ParametersDefinitionProperty {

    private ParameterDefinitions parameterDefinitions;

    public ParametersDefinitionProperty() {
    }

    public ParametersDefinitionProperty(ParameterDefinitions pd) {
        this.parameterDefinitions = pd;
    }

    public ParameterDefinitions getParameterDefinitions() {
        return parameterDefinitions;
    }

    public void setParameterDefinitions(ParameterDefinitions parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }
}
