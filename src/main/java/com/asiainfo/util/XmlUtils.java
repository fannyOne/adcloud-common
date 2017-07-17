package com.asiainfo.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XmlUtils {

    private final static Log LOG = LogFactory.getLog(XmlUtils.class);

    private static HashMap UNMARSHAL_METHOD_MAP = new HashMap();

    private XmlUtils() {
    }

    public static Document createJobDocumentFrom(InputStream inputStream) {

        final SAXBuilder builder = new SAXBuilder();

        final Document document;
        try {
            document = builder.build(inputStream);
        } catch (final JDOMException e) {
            LOG.warn("Failed to parse XML in input stream");
            LOG.warn("Exception follows:", e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (final IOException e) {
            LOG.warn("I/O error occurred while reading input stream");
            LOG.warn("Exception follows:", e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return document;

    }

    public static Element findSingleElementInDocumentByXPath(Document document, String xPath) {

        final XPathFactory xPathFactory = XPathFactory.instance();
        final XPathExpression<Element> xPathExpression = xPathFactory.compile(xPath, Filters.element());
        final Element element = xPathExpression.evaluateFirst(document);

        if (element == null) {
            throw new XmlUtilsException("Document does not contain element on path: " + xPath);
        }

        return element;

    }

    public static Map<String, String> xml2Map(String xmlStr, boolean flag)
        throws JDOMException, IOException {
        Map<String, String> rtnMap = new HashMap<String, String>();
        if (!("").equals(xmlStr) && xmlStr != null) {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new StringReader(xmlStr));
            // 得到根节点
            Element root = doc.getRootElement();
            String rootName = root.getName();
            rtnMap.put("project", rootName);
            // 调用递归函数，得到所有最底层元素的名称和值，加入map中
            convert(root, rtnMap, rootName, flag);
        }
        return rtnMap;
    }

    public static Map<String, String> xml2Map(String xmlStr) throws JDOMException, IOException {
        return xml2Map(xmlStr, true);
    }

    public static Map<String, String> treexml2Map(String xmlStr) throws JDOMException, IOException {
        return xml2Map(xmlStr, false);
    }

    public static void convert(Element e, Map<String, String> map,
                               String lastname, boolean isConnect) {
        List children = e.getChildren();
        Iterator it = children.iterator();
        while (it.hasNext()) {
            Element child = (Element) it.next();
            String name = child.getName();
            // 如果有子节点，则递归调用
            if (isConnect) {
                if (child.getChildren().size() > 0) {
                    convert(child, map, lastname + "$" + child.getName(), true);
                } else {
                    // 如果没有子节点，则把值加入map
                    map.put(lastname + "$" + name, child.getText());
                    // 如果该节点有属性，则把所有的属性值也加入map
                }
            } else {
                if (child.getChildren().size() > 0) {
                    convert(child, map, child.getName(), false);
                } else {
                    // 如果没有子节点，则把值加入map
                    map.put(name, child.getText());
                    // 如果该节点有属性，则把所有的属性值也加入map
                }
            }
        }
    }


    public static Document string2Doc(String xmlStr) throws Exception {
        java.io.Reader in = new StringReader(xmlStr);
        Document doc = (new SAXBuilder()).build(in);
        return doc;
    }

    public static String toXMLString(Object obj) {
        XStream xstream = new XStream(new DomDriver("utf8"));
        xstream.processAnnotations(obj.getClass()); // 识别obj类中的注解
        // 以压缩的方式输出XML
        StringWriter sw = new StringWriter();
        xstream.marshal(obj, new CompactWriter(sw));
        return sw.toString();
    }

    @SuppressWarnings("serial")
    public static class XmlUtilsException extends RuntimeException {
        public XmlUtilsException(String message) {
            super(message);
        }
    }


}
