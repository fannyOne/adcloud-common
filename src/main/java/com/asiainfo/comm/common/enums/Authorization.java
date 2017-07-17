package com.asiainfo.comm.common.enums;

/**
 * Created by zhenghp on 2016/9/9.
 */
public enum Authorization {
    ALL(AuthorizationResource.ALL, AuthorizationType.ALL),
    GROUP_ALL(AuthorizationResource.GROUP_ALL, AuthorizationType.ALL),
    RELEASE_ROLLBACK_OPER(AuthorizationResource.RELEASE_ROLLBACK, AuthorizationType.OPER),
    RELEASE_PLANJAR_OPER(AuthorizationResource.RELEASE_PLANJAR, AuthorizationType.OPER),
    PIPE_TEST_OPER(AuthorizationResource.PIPE_TEST, AuthorizationType.OPER),
    PIPE_PROD_OPER(AuthorizationResource.PIPE_PROD, AuthorizationType.OPER),
    PIPELINE_OPER(AuthorizationResource.PIPELINE, AuthorizationType.OPER);


    private AuthorizationResource resource;
    private AuthorizationType type;

    private Authorization(AuthorizationResource resource, AuthorizationType operation) {
        this.resource = resource;
        this.type = operation;
    }

    public static Authorization getAuthorization(AuthorizationResource resource, AuthorizationType operation) {
        for (Authorization param : values()) {
            if (param.getResource().equals(resource) && param.getType().equals(operation)) {
                return param;
            }
        }
        return null;
    }

    public AuthorizationResource getResource() {
        return resource;
    }

    public AuthorizationType getType() {
        return type;
    }

}
