package com.asiainfo.comm.module.models.functionModels;

import lombok.Data;

/**
 * Created by YangRY
 * 2016/7/12 0012.
 */
@Data
public class GitLabUser {
    String username;
    String displayName;
    Long id;
    String email;
    String state;
    String createdAt;
    String bio;
    String skype;
    String linkedin;
    String twitter;
    String websiteUrl;
    Long themeId;
    Long colorSchemeId;
    Boolean isAdmin;
    String avatarUrl;
    Boolean canCreateGroup;
    String currentSignInAt;
    Boolean twoFactorEnabled;
}
