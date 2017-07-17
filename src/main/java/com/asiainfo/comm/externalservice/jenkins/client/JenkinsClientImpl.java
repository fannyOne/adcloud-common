package com.asiainfo.comm.externalservice.jenkins.client;

import com.asiainfo.util.XmlUtils;
import com.asiainfo.comm.externalservice.jenkins.auth.User;
import com.asiainfo.comm.externalservice.jenkins.auth.UserImpl;
import com.asiainfo.comm.externalservice.jenkins.client.exceptions.JenkinsException;
import com.asiainfo.comm.externalservice.jenkins.client.exceptions.NoJenkinsServerException;
import com.asiainfo.comm.externalservice.jenkins.client.exceptions.NoSuchJobException;
import com.asiainfo.comm.externalservice.jenkins.client.exceptions.NoSuchUserException;
import com.asiainfo.comm.externalservice.jenkins.guice.JenkinsUrl;
import com.asiainfo.comm.externalservice.jenkins.jobs.Job;
import com.asiainfo.comm.externalservice.jenkins.jobs.JobImpl;
import com.google.inject.Inject;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jdom2.Document;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class JenkinsClientImpl implements JenkinsClient {

    private static final String JENKINS_VERSION_HEADER_NAME = "X-Jenkins";

    private static final Log LOG = LogFactory.getLog(JenkinsClientImpl.class);

    private final URL endpoint;
    private final HttpRestClient client;

    @Inject
    JenkinsClientImpl(HttpRestClient client, @JenkinsUrl URL endpoint) {

        LOG.trace("Initializing Jenkins client for endpoint: " + endpoint.toExternalForm());

        this.endpoint = checkNotNull(endpoint, "endpoint");
        this.client = checkNotNull(client, "client");

        // validateServerOnEndpoint();
    }

    /**
     * <p>Title: getBuildLog</p>
     * <p>Description: 通过ssh获取构建的代码清单</p>
     *
     * @param serverIp
     * @param serverUsername
     * @param serverPass
     * @param jobname
     * @param num
     * @return
     * @author bianwf
     * @date 2016年5月11日 下午4:56:39
     * @see JenkinsClient#getBuildLog(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long)
     */
    public static String getBuildLog(String serverIp, String serverUsername, String serverPass, String jobname, long num) {
        // TODO Auto-generated method stub
        String ret = "";
        String cmd = "cd jenkins/jobs/" + jobname + "/builds/" + num + " \n cat log";
        try {
            StringBuffer sf = ShellCommand.runSSH(serverIp, serverUsername, serverPass, cmd);
            if (sf != null) {
                ret = sf.toString();
                if (ret.indexOf("Date") > 0) {
                    ret = ret.substring(ret.indexOf("Date"));
                }
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("获取jenkins日志失败" + e);
            }
        }
        return ret;
    }

    @SuppressWarnings("unused")
    private void validateServerOnEndpoint() {
        String url = endpoint.toExternalForm() + "/login";

        LOG.trace("Validating Jenkins server on endpoint: " + url);
        HttpRestResponse response = client.get(url);

        if (response.isOk() && response.hasHeader(JENKINS_VERSION_HEADER_NAME)) {
            HttpRestResponse.Header header = response.getHeader(JENKINS_VERSION_HEADER_NAME);
            if (JenkinsVersion.SUPPORTED_JENKINS_VERSION.equals(header.getValue())) {
                LOG.trace("Jenkins server validated on endpoint: " + endpoint);
                return;
            }
        }

        LOG.error("No Jenkins server found on endpoint: " + url + "   response: " + response);
        throw new NoJenkinsServerException(url);
    }

    @Override
    public URL getJenkinsEndpoint() {
        return endpoint;
    }

    @Override
    public Job createJob(final String name, final String scmUrl) {

        LOG.trace("Creating job :" + name + "  -" + scmUrl);

        checkArgument(isNotEmpty(name), "name must be non-empty");
        checkArgument(isNotEmpty(scmUrl), "scmUrl must be non-empty");

        final Job job = new JobImpl(name);
        job.setScmUrl(scmUrl);
        job.clearNotificationRecipients();
        final String url = endpoint.toExternalForm() + "/createItem?name=" + name;
        final String xml = job.asXml();

        LOG.trace("Creating job ...");
        HttpRestResponse response = client.post(url, "application/xml", xml);

        if (response.isOk()) {
            response.consume();
            return job;
        } else {
            String message = "Error occurred while attempting to create job: " + response.getStatusLine();
            LOG.error(message);
            throw new JenkinsException(message);
        }

    }

    @Override
    public void buildJob(final Job job) {
        LOG.trace("build job :" + job.getName());

        final String url = endpoint.toExternalForm() + "/job/" + job.getName() + "/build";

        LOG.trace("build job ...");
        HttpRestResponse response = client.post(url, "text/plain", "");
        response.consume();

        if (!response.isFound() && HttpStatus.SC_CREATED != response.getStatusCode()) {
            String message = "Failed to build job: " + response.getStatusLine();
            LOG.error(message);
            throw new JenkinsException(message);
        }
    }

    @Override
    public Job retrieveJob(final String name) {

        LOG.trace("Retrieving job " + name);

        checkArgument(isNotEmpty(name), "name must be non-empty");

        final String url = urlForJob(name);

        LOG.trace("Retrieving config.xml ...");
        HttpRestResponse response = client.get(url);

        if (response.isOk()) {
            String xml = response.getContents();
            return JobImpl.fromXml(name, xml);
        } else if (response.isNotFound()) {
            response.consume();
            throw new NoSuchJobException(name);
        } else {
            response.consume();
            String message = "Error while attempting to retrieve job config.xml: " + response.getStatusLine();
            LOG.error(message);
            throw new JenkinsException(message);
        }

    }

    @Override
    public void updateJob(final Job job, String contents) {

        LOG.trace("Updating job: " + job);

        checkNotNull(job, "job");

        final String url = urlForJob(job);
        if (StringUtils.isEmpty(contents)) {
            contents = job.asXml();
        }
        LOG.trace("Creating job from XML ...");
        HttpRestResponse response = client.post(url, "application/xml;charset=UTF-8", contents);
        response.consume();

        if (!response.isOk()) {
            String message = "Failed to update job config.xml: " + response.getStatusLine();
            LOG.error(message);
            throw new JenkinsException(message);
        }
    }

    @Override
    public void deleteJob(final Job job) {

        LOG.trace("Deleting job " + job);

        final String url = endpoint.toExternalForm() + "/job/" + job.getName() + "/doDelete";

        LOG.trace("Deleting job ...");
        HttpRestResponse response = client.post(url, "text/plain", "");
        response.consume();

        if (!response.isFound()) {
            String message = "Failed to delete job: " + response.getStatusLine();
            LOG.error(message);
            throw new JenkinsException(message);
        }

    }

    private String urlForJob(final String name) {
        return endpoint.toExternalForm() + "/job/" + name + "/config.xml";
    }

    private String urlForJob(Job job) {
        return urlForJob(job.getName());
    }

    @Override
    public User createUser(String userName, String password, String email, String fullName) {
        LOG.trace("Creating user: " + userName + "   -" + email + "   -" + fullName);

        String url = endpoint.toExternalForm() + "/securityRealm/createAccountByAdmin";

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", userName));
        params.add(new BasicNameValuePair("password1", password));
        params.add(new BasicNameValuePair("password2", password));
        params.add(new BasicNameValuePair("fullname", fullName));
        params.add(new BasicNameValuePair("email", email));

        HttpRestResponse response = client.postForm(url, params);

        if (response.isFound()) {
            return retrieveUser(userName);
        } else {
            LOG.error("Failed to create user: " + response.getStatusLine());
            throw new JenkinsException("Failed to create user: " + response.getStatusLine());
        }
    }

    @Override
    public User retrieveUser(String userName) throws NoSuchUserException {
        LOG.trace("Retrieving user data: " + userName);

        String url = endpoint.toExternalForm() + "/user/" + userName + "/api/xml";

        HttpRestResponse response = client.get(url);

        if (response.isOk()) {
            return UserImpl.fromXml(response.getContents());
        } else {
            LOG.warn("Failed to retrieve user" + response.getStatusLine());
            throw new JenkinsException("Failed to retrieve user: " + response.getStatusLine());
        }
    }

    @Override
    public User updateUser() throws NoSuchUserException {
        throw new NotImplementedException();
    }

    @Override
    public void deleteUser(User user) throws NoSuchUserException {
        LOG.trace("Deleting user:" + user);

        String url = endpoint.toExternalForm() + "/user/" + user.getName() + "/doDelete";

        HttpRestResponse response = client.postForm(url, new ArrayList<NameValuePair>());

        if (!response.isFound()) {
            LOG.trace("Failed to delete user:" + response.getStatusLine());
            throw new JenkinsException("Failed to delete user: " + response.getStatusLine());
        }
    }

    @Override
    public void close() {
        client.close();
    }

    /**
     * <p>Description: </p> 获取当前构建的次数
     *
     * @param job
     * @return
     * @author bianwf
     * @date 2016年3月16日 下午2:48:23
     * @see JenkinsClient#getCompletedBuildNum(Job)
     */
    @Override
    public String getCompletedBuildNum(final Job job) {
        // TODO Auto-generated method stub
        if (LOG.isDebugEnabled()) {
            LOG.debug("获取jenkins工作情况");
        }
        String ret = "";
        try {
            String url = endpoint.toExternalForm() + "/job/" + job.getName()
                + "/api/xml";
            HttpRestResponse response = client.get(url);
            if (response.isOk()) {
                Map hmap;
                String xml = response.getContents();
                hmap = XmlUtils.xml2Map(xml);
                if (hmap != null) {
                    ret = "" + hmap.get("freeStyleProject$nextBuildNumber");
                }
            } else {
                response.consume();
                String message = "Error while get Jenkin BuildNum : "
                    + response.getStatusLine();
                LOG.error(message);
            }
        } catch (JDOMException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("获取当前构建结果" + e);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("获取当前构建结果" + e);
            }
        }
        return ret;
    }

    @Override
    public String getCurBuildNum(final Job job) {
        // TODO Auto-generated method stub
        if (LOG.isDebugEnabled()) {
            LOG.debug("获取jenkins工作情况");
        }
        String ret = "";
        try {
            String url = endpoint.toExternalForm() + "/job/" + job.getName()
                + "/api/xml";
            HttpRestResponse response = client.get(url);
            if (response.isOk()) {
                Map hmap;
                String xml = response.getContents();
                hmap = XmlUtils.xml2Map(xml);
                if (hmap != null) {
                    ret = "" + hmap.get("freeStyleProject$lastCompletedBuild$number");
                }
            } else {
                response.consume();
                String message = "Error while get Jenkin BuildNum : "
                    + response.getStatusLine();
                LOG.error(message);
            }
        } catch (JDOMException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("获取当前构建结果" + e);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("获取当前构建结果" + e);
            }
        }
        return ret;
    }

    /**
     * <p>Title: getBuildResult</p>
     * <p>Description: </p>
     *
     * @param jobname
     * @param num     第几次构建
     * @return true 表示成功 false 表示失败
     * @throws Exception
     * @author bianwf
     * @date 2016年3月16日 上午9:31:24
     */
    @Override
    public String getBuildResult(final String jobname, long num) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("获取构造结果");
        }
        String ret = "";
        String url = endpoint.toExternalForm() + "/job/" + jobname + "/" + num + "/api/xml";
        try {
            HttpRestResponse response = client.get(url);
            if (response.isOk()) {
                Map hmap;
                String xml = response.getContents();
                hmap = XmlUtils.xml2Map(xml);
                if (hmap != null) {
                    ret = "" + hmap.get("freeStyleBuild$result");
                }
            } else {
                response.consume();
                String message = "Error while get Jenkin BuildResult : "
                    + response.getStatusLine();
                if (LOG.isErrorEnabled()) {
                    LOG.error(message);
                }
            }
        } catch (JDOMException e) {
            ret = "FAILURE";
            if (LOG.isErrorEnabled()) {
                LOG.error("获取构建结果失败" + e);
            }
        } catch (IOException e) {
            ret = "FAILURE";
            if (LOG.isErrorEnabled()) {
                LOG.error("获取构建结果失败" + e);
            }
        }
        return ret;
    }

    @Override
    public Job createJob(String name, Document document) {
        checkArgument(isNotEmpty(name), "name must be non-empty");
        final Job job = new JobImpl(name, document);
        final String url = endpoint.toExternalForm() + "/createItem?name=" + name;
        final String xml = job.asXml();

        LOG.trace("Creating job ...");
        HttpRestResponse response = client.post(url, "application/xml", xml);

        if (response.isOk()) {
            response.consume();
            return job;
        } else {
            String message = "Error occurred while attempting to create job: " + response.getStatusLine();
            LOG.error(message);
            throw new JenkinsException(message);
        }
    }

    @Override
    public String stopJob(final String jobname, long num) {
        checkArgument(isNotEmpty(jobname), "name must be non-empty");

        final String url = endpoint.toExternalForm() + "/job/" + jobname + "/" + num + "/stop";

        LOG.error("stop job ...url=" + url);
        HttpRestResponse response = client.post(url, "text/plain", "");
        response.consume();
        if (!response.isFound() && HttpStatus.SC_CREATED != response.getStatusCode()) {
            String message = "Failed to stop job: " + response.getStatusLine();
            LOG.error(message);
            throw new JenkinsException(message);
        }
        LOG.error("stop job ...return=" + response);
        return "";
    }
}
