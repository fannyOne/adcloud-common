package com.asiainfo.comm.externalservice.jenkins.auth;

import com.asiainfo.util.XmlUtils;
import com.asiainfo.util.XmlUtils.XmlUtilsException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class UserImpl implements User {

    private final static Log LOG = LogFactory.getLog(UserImpl.class);
    private final String name;
    private final String email;

    public UserImpl(final String name, final String email) {

        checkArgument(isNotEmpty(name), "name must be non-empty");

        this.name = name;
        this.email = email;

    }

    public static User fromXml(String xml) {
        LOG.trace("Parsing User from xml: " + xml);

        InputStream contents = IOUtils.toInputStream(xml);

        Document document = XmlUtils.createJobDocumentFrom(contents);

        Element idElement = XmlUtils.findSingleElementInDocumentByXPath(document, "//user/id");
        String name = idElement.getText();

        String email;
        try {
            Element emailElement = null;
            emailElement = XmlUtils.findSingleElementInDocumentByXPath(document, "//user/property/address");
            email = emailElement.getText();
        } catch (XmlUtilsException e) {
            email = null;
        }

        return new UserImpl(name, email);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }

        if (this == that) {
            return true;
        }

        if (that instanceof User) {
            return getName().equals(((User) that).getName());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(name)
            .hashCode();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
