package com.asiainfo.comm.common.pojo.pojoExt;

import java.util.List;

/**
 * Created by yangry on 2016/6/16 0016.
 */
public class EnvExtPojo {
    private String name;
    private List<PipelineExtPojo> pipeline;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PipelineExtPojo> getPipeline() {
        return pipeline;
    }

    public void setPipeline(List<PipelineExtPojo> pipeline) {
        this.pipeline = pipeline;
    }
}
