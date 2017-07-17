package com.asiainfo.auth.sso.gitlib.api;

import com.asiainfo.auth.sso.gitlib.api.http.GitlabHTTPRequestor;
import com.asiainfo.auth.sso.gitlib.api.http.Query;
import com.asiainfo.auth.sso.gitlib.api.models.*;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Gitlab API Wrapper class
 *
 * @author @timols
 */
public class GitlabAPI {
    public static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final String API_NAMESPACE = "/api/v3";
    private final String _hostUrl;
    private final String _apiToken;
    private boolean _ignoreCertificateErrors = false;

    public GitlabAPI(String hostUrl, String apiToken) {
        _hostUrl = hostUrl.endsWith("/") ? hostUrl.replaceAll("/$", "") : hostUrl;
        _apiToken = apiToken;
    }

    public static GitlabSession connect(String hostUrl, String username, String password) throws IOException {
        String tailUrl = GitlabSession.URL;
        GitlabAPI api = connect(hostUrl, null);
        return api.dispatch().with("login", username).with("password", password).to(tailUrl, GitlabSession.class);
    }

    public static GitlabAPI connect(String hostUrl, String apiToken) {
        return new GitlabAPI(hostUrl, apiToken);
    }

    public GitlabAPI ignoreCertificateErrors(boolean ignoreCertificateErrors) {
        _ignoreCertificateErrors = ignoreCertificateErrors;
        return this;
    }

    public GitlabHTTPRequestor retrieve() {
        return new GitlabHTTPRequestor(this);
    }

    public GitlabHTTPRequestor dispatch() {
        return new GitlabHTTPRequestor(this).method("POST");
    }

    public boolean isIgnoreCertificateErrors() {
        return _ignoreCertificateErrors;
    }

    public URL getAPIUrl(String tailAPIUrl) throws IOException {
        if (_apiToken != null) {
            tailAPIUrl = tailAPIUrl + (tailAPIUrl.indexOf('?') > 0 ? '&' : '?') + "private_token=" + _apiToken;
        }

        if (!tailAPIUrl.startsWith("/")) {
            tailAPIUrl = "/" + tailAPIUrl;
        }
        return new URL(_hostUrl + API_NAMESPACE + tailAPIUrl);
    }

    public URL getUrl(String tailAPIUrl) throws IOException {
        if (!tailAPIUrl.startsWith("/")) {
            tailAPIUrl = "/" + tailAPIUrl;
        }

        return new URL(_hostUrl + tailAPIUrl);
    }

    public GitlabGroup getGroup(Integer groupId) throws IOException {
        String tailUrl = GitlabGroup.URL + "/" + groupId;
        return retrieve().to(tailUrl, GitlabGroup.class);
    }

    public List<GitlabGroup> getGroups() throws IOException {
        String tailUrl = GitlabGroup.URL;
        return retrieve().getAll(tailUrl, GitlabGroup[].class);
    }

    /**
     * Gets all members of a Group
     *
     * @param group The GitLab Group
     * @return The Group Members
     */
    public List<GitlabGroupMember> getGroupMembers(GitlabGroup group) throws IOException {
        return getGroupMembers(group.getId());
    }

    /**
     * Gets all members of a Group
     *
     * @param groupId The id of the GitLab Group
     * @return The Group Members
     */
    public List<GitlabGroupMember> getGroupMembers(Integer groupId) throws IOException {
        String tailUrl = GitlabGroup.URL + "/" + groupId + GitlabGroupMember.URL;
        return Arrays.asList(retrieve().to(tailUrl, GitlabGroupMember[].class));
    }

//    /**
//     * Creates a Group
//     *
//     * @param name The name of the group. The
//     *             name will also be used as the path
//     *             of the group.
//     * @return The GitLab Group
//     */
//    public GitlabGroup createGroup(String name) throws IOException {
//        return createGroup(name, name);
//    }

//    /**
//     * Creates a Group
//     *
//     * @param name The name of the group
//     * @param path The path for the group
//     * @return The GitLab Group
//     */
//    public GitlabGroup createGroup(String name, String path) throws IOException {
//        return createGroup(name, path, null, null);
//    }

//    /**
//     * Creates a Group
//     *
//     * @param name       The name of the group
//     * @param path       The path for the group
//     * @param ldapCn     LDAP Group Name to sync with, null otherwise
//     * @param ldapAccess Access level for LDAP group members, null otherwise
//     * @return The GitLab Group
//     */
//    public GitlabGroup createGroup(String name, String path, String ldapCn, GitlabAccessLevel ldapAccess) throws IOException {
////// TODO: 2016-07-06
//        /**
//         * FIXME:GUOJIAN menu int or string ?
//         */
//        Query query = new Query().append("name", name).append("path", path).appendIf("ldap_cn", ldapCn).appendIf("ldap_access", ldapAccess.name());
//
//        String tailUrl = GitlabGroup.URL + query.toString();
//
//        return dispatch().to(tailUrl, GitlabGroup.class);
//    }

    public GitlabProject getProject(Integer projectId) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + projectId;
        return retrieve().to(tailUrl, GitlabProject.class);
    }

    public List<GitlabProject> getProjects() throws IOException {
        String tailUrl = GitlabProject.URL;
        return retrieve().getAll(tailUrl, GitlabProject[].class);
    }

    public List<GitlabProject> getAllProjects() throws IOException {
        String tailUrl = GitlabProject.URL + "/all";
        return retrieve().getAll(tailUrl, GitlabProject[].class);
    }

    public List<GitlabProject> getOwned() throws IOException {
        String tailUrl = GitlabProject.URL + "/owned";
        return retrieve().getAll(tailUrl, GitlabProject[].class);
    }

    /**
     * Creates a private Project
     *
     * @param name The name of the project
     * @return The GitLab Project
     */
    public GitlabProject createProject(String name) throws IOException {
        return createProject(name, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Creates a Project
     *
     * @param name                 The name of the project
     * @param namespaceId          The Namespace for the new project, otherwise null indicates to use the GitLab default (user)
     * @param description          A description for the project, null otherwise
     * @param issuesEnabled        Whether Issues should be enabled, otherwise null indicates to use GitLab default
     * @param wallEnabled          Whether The Wall should be enabled, otherwise null indicates to use GitLab default
     * @param mergeRequestsEnabled Whether Merge Requests should be enabled, otherwise null indicates to use GitLab default
     * @param wikiEnabled          Whether a Wiki should be enabled, otherwise null indicates to use GitLab default
     * @param snippetsEnabled      Whether Snippets should be enabled, otherwise null indicates to use GitLab default
     * @param publik               Whether the project is public or private, if true same as setting visibilityLevel = 20, otherwise null indicates to use GitLab default
     * @param visibilityLevel      The visibility level of the project, otherwise null indicates to use GitLab default
     * @param importUrl            The Import URL for the project, otherwise null
     * @return the Gitlab Project
     */
    public GitlabProject createProject(String name, Integer namespaceId, String description, Boolean issuesEnabled, Boolean wallEnabled,
                                       Boolean mergeRequestsEnabled, Boolean wikiEnabled, Boolean snippetsEnabled, Boolean publik, Integer visibilityLevel, String importUrl)
        throws IOException {
        Query query = new Query().append("name", name).appendIf("namespace_id", namespaceId).appendIf("description", description)
            .appendIf("issues_enabled", issuesEnabled).appendIf("wall_enabled", wallEnabled)
            .appendIf("merge_requests_enabled", mergeRequestsEnabled).appendIf("wiki_enabled", wikiEnabled)
            .appendIf("snippets_enabled", snippetsEnabled).appendIf("public", publik).appendIf("visibility_level", visibilityLevel)
            .appendIf("import_url", importUrl);

        String tailUrl = GitlabProject.URL + query.toString();

        return dispatch().to(tailUrl, GitlabProject.class);
    }

    public GitlabProject createUserProject(Integer userId, String name) throws IOException {
        return createUserProject(userId, name, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Creates a Project for a specific User
     *
     * @param userId               The id of the user to create the project for
     * @param name                 The name of the project
     * @param description          A description for the project, null otherwise
     * @param defaultBranch        The default branch for the project, otherwise null indicates to use GitLab default (master)
     * @param issuesEnabled        Whether Issues should be enabled, otherwise null indicates to use GitLab default
     * @param wallEnabled          Whether The Wall should be enabled, otherwise null indicates to use GitLab default
     * @param mergeRequestsEnabled Whether Merge Requests should be enabled, otherwise null indicates to use GitLab default
     * @param wikiEnabled          Whether a Wiki should be enabled, otherwise null indicates to use GitLab default
     * @param snippetsEnabled      Whether Snippets should be enabled, otherwise null indicates to use GitLab default
     * @param publik               Whether the project is public or private, if true same as setting visibilityLevel = 20, otherwise null indicates to use GitLab default
     * @param visibilityLevel      The visibility level of the project, otherwise null indicates to use GitLab default
     * @return The GitLab Project
     */
    public GitlabProject createUserProject(Integer userId, String name, String description, String defaultBranch, Boolean issuesEnabled,
                                           Boolean wallEnabled, Boolean mergeRequestsEnabled, Boolean wikiEnabled, Boolean snippetsEnabled, Boolean publik, Integer visibilityLevel)
        throws IOException {
        Query query = new Query().append("name", name).appendIf("description", description).appendIf("default_branch", defaultBranch)
            .appendIf("issues_enabled", issuesEnabled).appendIf("wall_enabled", wallEnabled)
            .appendIf("merge_requests_enabled", mergeRequestsEnabled).appendIf("wiki_enabled", wikiEnabled)
            .appendIf("snippets_enabled", snippetsEnabled).appendIf("public", publik).appendIf("visibility_level", visibilityLevel);

        String tailUrl = GitlabProject.URL + "/user/" + userId + query.toString();

        return dispatch().to(tailUrl, GitlabProject.class);
    }

    public List<GitlabMergeRequest> getOpenMergeRequests(GitlabProject project) throws IOException {
        List<GitlabMergeRequest> allMergeRequests = getAllMergeRequests(project);
        List<GitlabMergeRequest> openMergeRequests = new ArrayList<GitlabMergeRequest>();

        for (GitlabMergeRequest mergeRequest : allMergeRequests) {
            if (mergeRequest.isMerged() || mergeRequest.isClosed()) {
                continue;
            }
            openMergeRequests.add(mergeRequest);
        }

        return openMergeRequests;
    }

    public List<GitlabMergeRequest> getMergeRequests(Integer projectId) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + projectId + GitlabMergeRequest.URL;
        return fetchMergeRequests(tailUrl);
    }

    /**
     * 描述: 更新merge request.
     *
     * @param request 合并请求信息
     * @return 合并请求信息
     * @throws IOException 异常
     * @author Administrator
     * <p>Sample: 该方法使用样例</p>
     * date        2014年7月9日
     * -----------------------------------------------------------
     * 修改人                                             修改日期                                   修改描述
     * Administrator                2014年7月9日               创建
     * -----------------------------------------------------------
     * @Version Ver1.0
     */
    public GitlabMergeRequest closeMergeRequestState(GitlabMergeRequest request) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + request.getProjectId() + "/merge_request/" + request.getId();
        GitlabHTTPRequestor requestor = retrieve().method("PUT");
        requestor.with("state_event", request.getState());

        return requestor.to(tailUrl, GitlabMergeRequest.class);
    }

    public List<GitlabMergeRequest> getMergeRequests(GitlabProject project) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabMergeRequest.URL;
        return fetchMergeRequests(tailUrl);
    }

    public List<GitlabMergeRequest> getAllMergeRequests(GitlabProject project) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabMergeRequest.URL;
        return retrieve().getAll(tailUrl, GitlabMergeRequest[].class);
    }

    public GitlabMergeRequest getMergeRequest(GitlabProject project, Integer mergeRequestId) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + "/merge_request/" + mergeRequestId;
        return retrieve().to(tailUrl, GitlabMergeRequest.class);
    }

    public List<GitlabNote> getNotes(GitlabMergeRequest mergeRequest) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + mergeRequest.getProjectId() + GitlabMergeRequest.URL + "/" + mergeRequest.getId() + GitlabNote.URL;

        GitlabNote[] notes = retrieve().to(tailUrl, GitlabNote[].class);
        return Arrays.asList(notes);
    }

    public List<GitlabNote> getAllNotes(GitlabMergeRequest mergeRequest) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + mergeRequest.getProjectId() + GitlabMergeRequest.URL + "/" + mergeRequest.getId() + GitlabNote.URL;

        return retrieve().getAll(tailUrl, GitlabNote[].class);

    }

    public List<GitlabCommit> getCommits(GitlabMergeRequest mergeRequest) throws IOException {
        Integer projectId = mergeRequest.getSourceProjectId();
        if (projectId == null) {
            projectId = mergeRequest.getProjectId();
        }

        Query query = new Query().append("ref_name", mergeRequest.getSourceBranch());

        String tailUrl = GitlabProject.URL + "/" + projectId + "/repository" + GitlabCommit.URL + query.toString();

        GitlabCommit[] commits = retrieve().to(tailUrl, GitlabCommit[].class);
        return Arrays.asList(commits);
    }

    public GitlabNote createNote(GitlabMergeRequest mergeRequest, String body) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + mergeRequest.getProjectId() + GitlabMergeRequest.URL + "/" + mergeRequest.getId() + GitlabNote.URL;

        return dispatch().with("body", body).to(tailUrl, GitlabNote.class);
    }

    public List<GitlabBranch> getBranches(GitlabProject project) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabBranch.URL;
        GitlabBranch[] branches = retrieve().to(tailUrl, GitlabBranch[].class);
        return Arrays.asList(branches);
    }

    public GitlabBranch getBranch(GitlabProject project, String branchName) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabBranch.URL + branchName;
        GitlabBranch branch = retrieve().to(tailUrl, GitlabBranch.class);
        return branch;
    }

    public void protectBranch(GitlabProject project, String branchName) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabBranch.URL + branchName + "/protect";
        retrieve().method("PUT").to(tailUrl, Void.class);
    }

    public void unprotectBranch(GitlabProject project, String branchName) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabBranch.URL + branchName + "/unprotect";
        retrieve().method("PUT").to(tailUrl, Void.class);
    }

    public List<GitlabProjectHook> getProjectHooks(GitlabProject project) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabProjectHook.URL;
        GitlabProjectHook[] hooks = retrieve().to(tailUrl, GitlabProjectHook[].class);
        return Arrays.asList(hooks);
    }

    public GitlabProjectHook getProjectHook(GitlabProject project, String hookId) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabProjectHook.URL + "/" + hookId;
        GitlabProjectHook hook = retrieve().to(tailUrl, GitlabProjectHook.class);
        return hook;
    }

    public GitlabProjectHook addProjectHook(GitlabProject project, String url) throws IOException {
        Query query = new Query().append("url", url);

        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabProjectHook.URL + query.toString();
        return dispatch().to(tailUrl, GitlabProjectHook.class);
    }

    public GitlabProjectHook editProjectHook(GitlabProject project, String hookId, String url) throws IOException {
        Query query = new Query().append("url", url);

        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabProjectHook.URL + "/" + hookId + query.toString();
        return retrieve().method("PUT").to(tailUrl, GitlabProjectHook.class);
    }

    public void deleteProjectHook(GitlabProject project, String hookId) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabProjectHook.URL + "/" + hookId;
        retrieve().method("DELETE").to(tailUrl, Void.class);
    }

    private List<GitlabMergeRequest> fetchMergeRequests(String tailUrl) throws IOException {
        GitlabMergeRequest[] mergeRequests = retrieve().to(tailUrl, GitlabMergeRequest[].class);
        return Arrays.asList(mergeRequests);
    }

    public List<GitlabIssue> getIssues(GitlabProject project) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabIssue.URL;
        return retrieve().getAll(tailUrl, GitlabIssue[].class);
    }

    public GitlabIssue getIssue(Integer projectId, Integer issueId) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + projectId + GitlabIssue.URL + "/" + issueId;
        return retrieve().to(tailUrl, GitlabIssue.class);
    }

    public GitlabIssue createIssue(int projectId, int assigneeId, int milestoneId, String labels, String description, String title)
        throws IOException {
        String tailUrl = GitlabProject.URL + "/" + projectId + GitlabIssue.URL;
        GitlabHTTPRequestor requestor = dispatch();
        applyIssue(requestor, assigneeId, milestoneId, labels, description, title);

        return requestor.to(tailUrl, GitlabIssue.class);
    }

    public GitlabIssue editIssue(int projectId, int issueId, int assigneeId, int milestoneId, String labels, String description, String title,
                                 GitlabIssue.Action action) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + projectId + GitlabIssue.URL + "/" + issueId;
        GitlabHTTPRequestor requestor = retrieve().method("PUT");
        applyIssue(requestor, assigneeId, milestoneId, labels, description, title);

        if (action != GitlabIssue.Action.LEAVE) {
            requestor.with("state_event", action.toString().toLowerCase());
        }

        return requestor.to(tailUrl, GitlabIssue.class);
    }

    private void applyIssue(GitlabHTTPRequestor requestor, int assigneeId, int milestoneId, String labels, String description,
                            String title) {

        requestor.with("title", title).with("description", description).with("labels", labels).with("milestone_id", milestoneId);

        if (assigneeId != 0) {
            requestor.with("assignee_id", assigneeId == -1 ? 0 : assigneeId);
        }
    }

    public List<GitlabNote> getNotes(GitlabIssue issue) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + issue.getProjectId() + GitlabIssue.URL + "/" + issue.getId() + GitlabNote.URL;
        return Arrays.asList(retrieve().to(tailUrl, GitlabNote[].class));
    }

    public GitlabNote createNote(Integer projectId, Integer issueId, String message) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + projectId + GitlabIssue.URL + "/" + issueId + GitlabNote.URL;
        return dispatch().with("body", message).to(tailUrl, GitlabNote.class);
    }

    public GitlabNote createNote(GitlabIssue issue, String message) throws IOException {
        return createNote(issue.getProjectId(), issue.getId(), message);
    }

    public List<GitlabMilestone> getMilestones(GitlabProject project) throws IOException {
        return getMilestones(project.getId());
    }

    public List<GitlabMilestone> getMilestones(Integer projectId) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + projectId + GitlabMilestone.URL;
        return Arrays.asList(retrieve().to(tailUrl, GitlabMilestone[].class));
    }

    public List<GitlabProjectMember> getProjectMembers(GitlabProject project) throws IOException {
        return getProjectMembers(project.getId());
    }

    public List<GitlabProjectMember> getProjectMembers(Integer projectId) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + projectId + GitlabProjectMember.URL;
        return Arrays.asList(retrieve().to(tailUrl, GitlabProjectMember[].class));
    }

    /**
     * This will fail, if the given namespace is a user and not a group
     *
     * @param namespace
     * @return
     * @throws IOException
     */
    public List<GitlabProjectMember> getNamespaceMembers(GitlabNamespace namespace) throws IOException {
        return getNamespaceMembers(namespace.getId());
    }

    /**
     * This will fail, if the given namespace is a user and not a group
     *
     * @param namespaceId
     * @return
     * @throws IOException
     */
    public List<GitlabProjectMember> getNamespaceMembers(Integer namespaceId) throws IOException {
        String tailUrl = GitlabNamespace.URL + "/" + namespaceId + GitlabProjectMember.URL;
        return Arrays.asList(retrieve().to(tailUrl, GitlabProjectMember[].class));
    }

    public GitlabSession getCurrentSession() throws IOException {
        String tailUrl = "/user";
        return retrieve().to(tailUrl, GitlabSession.class);
    }

    /**
     * Get a list of tags in specific project
     *
     * @param projectId
     * @return
     * @throws IOException on gitlab api call error
     */
    public List<GitlabTag> getTags(Serializable projectId) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + sanitizeProjectId(projectId) + GitlabTag.URL;
        GitlabTag[] tags = retrieve().to(tailUrl, GitlabTag[].class);
        return Arrays.asList(tags);
    }

    /**
     * Get a list of tags in specific project
     *
     * @param project
     * @return
     * @throws IOException on gitlab api call error
     */
    public List<GitlabTag> getTags(GitlabProject project) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabTag.URL;
        GitlabTag[] tags = retrieve().to(tailUrl, GitlabTag[].class);
        return Arrays.asList(tags);
    }

    //    /**
//     * Get a list of tags in specific project
//     *
//     * @param project
//     * @return
//     * @throws IOException on gitlab api call error
//     */
    public List<GitlabTag> getTags(String projectId) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + projectId + GitlabTag.URL;
        GitlabTag[] tags = retrieve().to(tailUrl, GitlabTag[].class);
        return Arrays.asList(tags);
    }

    /**
     * Create tag in specific project
     *
     * @param projectId
     * @param tagName
     * @param ref
     * @param message
     * @param releaseDescription
     * @return
     * @throws IOException on gitlab api call error
     */
    public GitlabTag addTag(Serializable projectId, String tagName, String ref, String message, String releaseDescription) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + sanitizeProjectId(projectId) + GitlabTag.URL;
        return dispatch()
            .with("tag_name", tagName)
            .with("ref", ref)
            .with("message", message)
            .with("release_description", releaseDescription)
            .to(tailUrl, GitlabTag.class);
    }

    /**
     * Create tag in specific project
     *
     * @param project
     * @param tagName
     * @param ref
     * @param message
     * @param releaseDescription
     * @return
     * @throws IOException on gitlab api call error
     */
    public GitlabTag addTag(GitlabProject project, String tagName, String ref, String message, String releaseDescription) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project.getId() + GitlabTag.URL;
        return dispatch()
            .with("tag_name", tagName)
            .with("ref", ref)
            .with("message", message)
            .with("release_description", releaseDescription)
            .to(tailUrl, GitlabTag.class);
    }

    /**
     * Create tag in specific project
     *
     * @param projectId
     * @param tagName
     * @param ref
     * @param message
     * @param releaseDescription
     * @return
     * @throws IOException on gitlab api call error
     */
    public GitlabTag addTag(String projectId, String tagName, String ref, String message, String releaseDescription) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + projectId + GitlabTag.URL;
        return dispatch()
            .with("tag_name", tagName)
            .with("ref", ref)
            .with("message", message)
            .with("release_description", releaseDescription)
            .to(tailUrl, GitlabTag.class);
    }

    /**
     * Delete tag in specific project
     *
     * @param projectId
     * @param tagName
     * @throws IOException on gitlab api call error
     */
    public void deleteTag(Serializable projectId, String tagName) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + sanitizeProjectId(projectId) + GitlabTag.URL + "/" + tagName;
        retrieve().method("DELETE").to(tailUrl, Void.class);
    }

    /**
     * Delete tag in specific project
     *
     * @param project
     * @param tagName
     * @throws IOException on gitlab api call error
     */
    public void deleteTag(GitlabProject project, String tagName) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + project + GitlabTag.URL + "/" + tagName;
        retrieve().method("DELETE").to(tailUrl, Void.class);
    }

    private String sanitizeProjectId(Serializable projectId) {
        if (!(projectId instanceof String) && !(projectId instanceof Number)) {
            throw new IllegalArgumentException("projectId needs to be of type String or Number");
        }

        try {
            return URLEncoder.encode(String.valueOf(projectId), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException((e));
        }
    }

    public List<GitlabCommit> getLastCommits(Serializable projectId, String branchOrTag) throws IOException {
        return getCommits(projectId, branchOrTag);
    }

    public List<GitlabCommit> getCommits(Serializable projectId,
                                         String branchOrTag) throws IOException {
        final Query query = new Query();
        if (branchOrTag != null) {
            query.append("ref_name", branchOrTag);
        }

//        if (pagination != null) {
//            query.mergeWith(pagination.asQuery());
//        }

        String tailUrl = GitlabProject.URL + "/" + sanitizeProjectId(projectId) +
            "/repository" + GitlabCommit.URL + query;
        final GitlabCommit[] commits = retrieve().to(tailUrl, GitlabCommit[].class);
        return Arrays.asList(commits);
    }

    public GitlabCommit getCommit(Serializable projectId, String commitHash) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + sanitizeProjectId(projectId) + "/repository/commits/" + commitHash;
        return retrieve().to(tailUrl, GitlabCommit.class);
    }

    public GitlabUser updateUser(Integer userId, String password)
        throws IOException {
        Query query = new Query()
            .append("password", password);

        String tailUrl = GitlabUser.URL + "/" + userId + query.toString();
        System.out.println("query.toString()==" + query.toString());

        return retrieve().method("PUT").to(tailUrl, GitlabUser.class);
    }

    public List<GitlabUser> findUsers(String emailOrUsername) throws IOException {
        List<GitlabUser> users = new ArrayList<GitlabUser>();
        if (emailOrUsername != null && !emailOrUsername.equals("")) {
            String tailUrl = GitlabUser.URL + "?search=" + emailOrUsername;
            GitlabUser[] response = retrieve().to(tailUrl, GitlabUser[].class);
            users = Arrays.asList(response);
        }
        return users;
    }
    // List commit diffs for a project ID and commit hash
    // GET /projects/:id/repository/commits/:sha/diff
//    public List<GitlabCommitDiff> getCommitDiffs(Serializable projectId, String commitHash) throws IOException {
//        return getCommitDiffs(projectId, commitHash, new Pagination());
//    }

    public List<GitlabCommitDiff> getCommitDiffs(Serializable projectId, String commitHash) throws IOException {
        String tailUrl = GitlabProject.URL + "/" + sanitizeProjectId(projectId) + "/repository/commits/" + commitHash +
            GitlabCommitDiff.URL;
        GitlabCommitDiff[] diffs = retrieve().to(tailUrl, GitlabCommitDiff[].class);
        System.out.println("tailUrl==" + tailUrl);
        return Arrays.asList(diffs);
    }

    public GitlabUser createUser(String email, String password, String username,
                                 String fullName) throws IOException {
        Query query = new Query()
            .append("email", email)
            .appendIf("password", password)
            .appendIf("username", username)
            .appendIf("name", fullName);
        String tailUrl = GitlabUser.USERS_URL + query.toString();

        return dispatch().to(tailUrl, GitlabUser.class);
    }
}
