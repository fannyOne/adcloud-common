package com.asiainfo.comm.externalservice.jenkins.jobconfig;

import com.asiainfo.comm.externalservice.jenkins.jobconfig.entity.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import org.springframework.util.Assert;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @version v 1.0 on 2016/7/13 19:03
 * @auther william.xu
 */
public class JobConfigUtil {

    private static XStream xstream;

    static {
        xstream = new XStream();
        xstream.autodetectAnnotations(true);
        xstream.processAnnotations(new java.lang.Class[]{
            JobConfig.class,
            HudsonNotificationProperty.class,
            Endpoint.class,
            ParametersDefinitionProperty.class,
            ParameterDefinitions.class,
            StringParameterDefinition.class,
            NullSCM.class,
            GitSCM.class,
            UserRemoteConfig.class,
            BranchSpec.class,
            SCMTrigger.class,
            TimerTrigger.class,
            ReverseBuildTrigger.class,
            Shell.class,
            HudsonSoundsNotifier.class,
            HudsonSoundsNotifier.SoundEvent.class,
            ExtendedEmailPublisher.class,
            SuccessTrigger.class,
            FailureTrigger.class,
            DevelopersRecipientProvider.class,
            ListRecipientProvider.class,
            UpstreamComitterRecipientProvider.class
        });
    }

    public static String toXMLString(JobConfig jobConfig) {
        Assert.notNull(jobConfig);
        Writer writer = new StringWriter();
        xstream.marshal(jobConfig, new PrettyPrintWriter(writer));
        return writer.toString();
    }

    public static JobConfig fromXML(Reader configXML) {
        Assert.notNull(configXML);
        return (JobConfig) xstream.fromXML(configXML);
    }

    public static JobConfig fromXMLString(String configXML) {
        Assert.notNull(configXML);
        return (JobConfig) xstream.fromXML(configXML);
    }
}
