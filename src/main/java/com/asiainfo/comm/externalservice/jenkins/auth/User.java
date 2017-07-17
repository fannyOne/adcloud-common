package com.asiainfo.comm.externalservice.jenkins.auth;

public interface User {

    /**
     * The user's display name.
     */
    String getName();

    /**
     * The user's email address.
     */
    String getEmail();

}
