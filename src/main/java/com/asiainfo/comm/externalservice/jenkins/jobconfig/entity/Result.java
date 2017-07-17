package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @version v 1.0 on 2016/7/13 17:28
 * @auther william.xu
 */
@XStreamAlias("hudson.model.Result")
public class Result {

    public static final Result SUCCESS = new Result("SUCCESS", BallColor.BLUE, 0, true);
    /**
     * The build had some errors but they were not fatal.
     * For example, some tests failed.
     */
    public static final Result UNSTABLE = new Result("UNSTABLE", BallColor.YELLOW, 1, true);
    /**
     * The build had a fatal error.
     */
    public static final Result FAILURE = new Result("FAILURE", BallColor.RED, 2, true);
    /**
     * The module was not built.
     * <p>
     * This status code is used in a multi-stage build (like maven2)
     * where a problem in earlier stage prevented later stages from building.
     */
    public static final Result NOT_BUILT = new Result("NOT_BUILT", BallColor.NOTBUILT, 3, false);
    /**
     * The build was manually aborted.
     * <p>
     * If you are catching {@link InterruptedException} and interpreting it as {@link #ABORTED},
     * you should check {@link Executor#abortResult()} instead (starting 1.417.)
     */
    public static final Result ABORTED = new Result("ABORTED", BallColor.ABORTED, 4, false);
    public int ordinal;
    public BallColor color;
    public boolean completeBuild;
    private String name;

    private Result(String name, BallColor color, int ordinal, boolean complete) {
        this.name = name;
        this.color = color;
        this.ordinal = ordinal;
        this.completeBuild = complete;
    }


}
