package com.asiainfo.comm.externalservice.jenkins.client;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

class HttpRestResponseImpl implements HttpRestResponse {

    private static final Log LOG = LogFactory.getLog(HttpRestResponseImpl.class);

    private final HttpResponse response;
    private final String contents;
    private Map<String, Header> headers;

    HttpRestResponseImpl(HttpResponse response) {
        this.response = checkNotNull(response);

        LOG.trace("Creating response object. Status: " + response.getStatusLine());

        headers = headersOf(response);
        contents = contentsOf(response.getEntity());
    }

    private static Map<String, Header> headersOf(HttpResponse response) {
        LOG.trace("Parsing headers...");

        final Map<String, String> headers = new HashMap<>();
        for (org.apache.http.Header header : response.getAllHeaders()) {
            String name = header.getName();
            String value = header.getValue();
            LOG.trace("Header: " + name + "-" + value);

            if (headers.containsKey(name)) {
                // The spec (rfc X) says that headers can be duplicated. So, handle
                // this special case: values should be considered a comma-separated
                // list for a single header.
                String oldValue = headers.get(name);
                headers.put(name, oldValue + "," + value);
            } else {
                headers.put(name, value);
            }
        }

        Map<String, Header> result = new HashMap<>();
        for (Entry<String, String> header : headers.entrySet()) {
            String name = header.getKey();
            String value = header.getValue();
            result.put(name, new HttpRestResponseHeaderImpl(name, value));
        }

        return result;
    }

    private static String contentsOf(HttpEntity entity) {
        LOG.trace("Streaming contents...");

        String contents;

        try {
            contents = EntityUtils.toString(entity, getContentCharSet(entity));
        } catch (ParseException e) {
            String message = "Header elements of POST response could not be parsed";
            LOG.error(message, e);
            throw new HttpRestClientException(message, e);
        } catch (IOException e) {
            String message = "Error occurred while reading network stream";
            LOG.error(message, e);
            throw new HttpRestClientException(message, e);
        } finally {
            EntityUtils.consumeQuietly(entity);
        }

        return contents;
    }

    public static String getContentCharSet(final HttpEntity entity)
        throws ParseException {

        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }

        if (StringUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }
        return charset;
    }

    @Override
    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public boolean isOk() {
        return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    }

    @Override
    public boolean isFound() {
        return response.getStatusLine().getStatusCode() == 302;
    }

    @Override
    public boolean isNotFound() {
        return response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND;
    }

    @Override
    public String getStatusLine() {
        return response.getStatusLine().toString();
    }

    private List<Header> getHeaders() {
        final Map<String, String> headers = new HashMap<>();
        for (org.apache.http.Header header : response.getAllHeaders()) {
            if (headers.containsKey(header.getName())) {
                headers.put(header.getName(), headers.get(header.getName()) + header.getValue());
            } else {
                headers.put(header.getName(), header.getValue());
            }
        }

        final List<Header> result = new ArrayList<>();
        for (Entry<String, String> header : headers.entrySet()) {
            result.add(new HttpRestResponseHeaderImpl(header.getKey(), header.getValue()));
        }

        return result;
    }

    @Override
    public boolean hasHeader(String name) {
        checkArgument(isNotEmpty(name), "name must be non-empty");

        return headers.containsKey(name);
    }

    @Override
    public Header getHeader(String name) {
        checkArgument(isNotEmpty(name), "name must be non-empty");

        return headers.get(name);
    }

    @Override
    public String getContents() {
        return contents;
    }

    @Override
    public void consume() {
        EntityUtils.consumeQuietly(response.getEntity());
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("statusLine", getStatusLine());
        builder.append("headers", getHeaders());
        return builder.toString();
    }

    private static class HttpRestResponseHeaderImpl implements Header {

        private String name;
        private String value;

        private HttpRestResponseHeaderImpl(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            final ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
            builder.append("name", name);
            builder.append("value", value);
            return builder.toString();
        }

    }

}
