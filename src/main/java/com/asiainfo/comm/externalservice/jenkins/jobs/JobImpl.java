package com.asiainfo.comm.externalservice.jenkins.jobs;

import com.asiainfo.util.XmlUtils;
import com.asiainfo.comm.externalservice.jenkins.auth.User;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.JobConfigUtil;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.entity.JobConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.*;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.asiainfo.util.XmlUtils.findSingleElementInDocumentByXPath;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class JobImpl implements Job {

    /**
     * If true, Jobs are created with limited permissions, specifically tailored
     * for use with DevHub.
     */
    private static final boolean DEVHUB = true;

    private static final Log LOG = LogFactory.getLog(JobImpl.class);
    private static final String XPATH_PROPERTIES_SECURITY = "//maven2-moduleset/properties/hudson.security.AuthorizationMatrixProperty";
    private static final String XPATH_NOTIFICATION_RECIPIENTS = "//maven2-moduleset/reporters/hudson.maven.reporters.MavenMailer/recipients";

    //	private final JobPermissionMatrix permissionMatrix;
    private static final String XPATH_SCM_GIT_URL = "//maven2-moduleset/scm/userRemoteConfigs/hudson.plugins.git.UserRemoteConfig/url";
    private final String name;
    private final Document document;

    public JobImpl(final String name) {
//		this(name, JobDocumentProvider.createDefaultJobDocument());
        this(name, null);
    }

    public JobImpl(final String name, final Document document) {
        this.name = name;
        this.document = document;

    }
//	public JobImpl(final String name, final Document document) {
//		checkArgument(isNotEmpty(name), "name must be non-empty");
//
//		this.name = name;
//		this.document = checkNotNull(document, "document must be non-null");
//        if(document!=null){
//            final Element element = findSingleElementInDocumentByXPath(document, XPATH_PROPERTIES_SECURITY);
//        }
////		permissionMatrix = JobPermissionMatrixImpl.fromElement(element);
//	}

    public static Job fromXml(final String name, final String xml) {
        LOG.trace("Creating job named  from xml ...:" + name);

        checkArgument(isNotEmpty(name), "name must be non-empty");
        checkArgument(isNotEmpty(xml), "xml must be non-empty");

        final InputStream is = IOUtils.toInputStream(xml);

        final Document document = XmlUtils.createJobDocumentFrom(is);

        return new JobImpl(name, document);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getScmUrl() {
        throw new NotImplementedException();
    }

    @Override
    public void setScmUrl(final String scmUrl) {
        LOG.trace("Setting SCM URL to: " + scmUrl);

        checkArgument(isNotEmpty(scmUrl), "scmUrl must be non-empty");

        final Element url = findSingleElementInDocumentByXPath(document, XPATH_SCM_GIT_URL);

        url.setContent(new Text(scmUrl));
    }

    @Override
    public void clearNotificationRecipients() {
        LOG.trace("Clearing notification recipient list...");

        final Element recipients = findSingleElementInDocumentByXPath(document, XPATH_NOTIFICATION_RECIPIENTS);

        recipients.removeContent();
    }

    @Override
    public void addNotificationRecipient(final User recipient) {
        LOG.trace("Adding additional notification recipient:" + recipient);

        checkNotNull(recipient, "recipient must be non-null");
        checkArgument(isNotEmpty(recipient.getEmail()), "recipient.email must be non-empty");
        checkArgument(recipient.getEmail().contains("@"), "recipient.email must contain @");

        final Element recipients = findSingleElementInDocumentByXPath(document, XPATH_NOTIFICATION_RECIPIENTS);

        final int contentSize = recipients.getContentSize();
        if (contentSize == 0) {
            recipients.setContent(new Text(recipient.getEmail()));
        } else if (contentSize == 1) {
            final Content content = recipients.getContent(0);
            final String value = content.getValue();
            recipients.setContent(new Text(value + " " + recipient.getEmail()));
        } else {
            throw new RuntimeException("Element on path " + XPATH_NOTIFICATION_RECIPIENTS + " contains multiple children. Single (text) element expected");
        }
    }

    @Override
    public void removeNotificationRecipient(User recipient) {
        LOG.trace("Removing notification recipient: " + recipient);

        checkNotNull(recipient, "recipient must be non-null");
        checkArgument(isNotEmpty(recipient.getEmail()), "recipient.email must be non-empty");
        checkArgument(recipient.getEmail().contains("@"), "recipient.email must contain @");

        Element recipients = findSingleElementInDocumentByXPath(document, XPATH_NOTIFICATION_RECIPIENTS);
        String text = recipients.getText();
        String[] strings = text.split(" ");

        StringBuilder newText = new StringBuilder();
        for (String string : strings) {
            if (!string.isEmpty() && !string.equals(recipient.getEmail())) {
                newText.append(string);
                newText.append(" ");
            }
        }

        recipients.setText(newText.toString().trim());
    }

    @Override
    public String asXml() {

        LOG.trace("Generating XML representation...");

        final XMLOutputter outputter = new XMLOutputter();
        final String xml = outputter.outputString(document);

        return xml;

    }

    //@Override
    public Map getJob() {
        final XMLOutputter outputter = new XMLOutputter();
        final String xml = outputter.outputString(document);
        try {
            Map hmap = XmlUtils.xml2Map(xml);
            return hmap;
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JobConfig getJobinfo() {
        final XMLOutputter outputter = new XMLOutputter();
        final String xml = outputter.outputString(document);
        JobConfig jobConfig = JobConfigUtil.fromXMLString(xml);
        return jobConfig;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);

        builder.append("name", name);

        return builder.toString();
    }

}
//	@Override
//	public List<User> getUsers() {
//		return permissionMatrix.getUsers();
//	}

//	@Override
//	public void addPermissionsForUser(User user) {
//		LOG.trace("Adding user with full permissions: "+ user);
//
//		checkNotNull(user, "user");
//
//		if (DEVHUB) {
//			addDevHubPermissionsForUser(user);
//		} else {
//			addFullPermissionsForUser(user);
//		}
//	}

//	@Override
//	public void removePermissionsForUser(User user) {
//		LOG.trace("Removing user: "+user);
//
//		checkNotNull(user, "user must be non-null");
//
//		permissionMatrix.removeAllPermissionsForUser(user);
//
//		Element authMatrix = findSingleElementInDocumentByXPath(document, XPATH_PROPERTIES_SECURITY);
//
//		Iterator<Element> iterator = authMatrix.getChildren().iterator();
//		while (iterator.hasNext()) {
//			Element permission = iterator.next();
//			if (permission.getText().endsWith(user.getName())) {
//				iterator.remove();
//			}
//		}
//
//	}

//	private void addDevHubPermissionsForUser(User user) {
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.JOB_DISCOVER);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.JOB_READ);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.JOB_WORKSPACE);
//	}

//	private void addFullPermissionsForUser(User user) {
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.JOB_BUILD);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.JOB_CANCEL);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.JOB_CONFIGURE);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.JOB_DELETE);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.JOB_DISCOVER);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.JOB_READ);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.JOB_WORKSPACE);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.RUN_DELETE);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.RUN_UPDATE);
//		permissionMatrix.addPermission(user, JobAuthMatrixPermission.SCM_TAG);
//	}
