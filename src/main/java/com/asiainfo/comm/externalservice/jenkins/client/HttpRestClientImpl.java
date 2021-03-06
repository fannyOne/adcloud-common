package com.asiainfo.comm.externalservice.jenkins.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.*;
import org.apache.http.protocol.HttpContext;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

class HttpRestClientImpl implements HttpRestClient {
    private final static Log LOG = LogFactory.getLog(HttpRestClientImpl.class);

    private final HttpClient client;
    private final HttpContext httpContext;
    private final HttpMethodFactory methodFactory;

    @Inject
    HttpRestClientImpl(HttpClient client, HttpContext httpContext, HttpMethodFactory methodFactory) {
        this.client = checkNotNull(client, "client");
        this.httpContext = checkNotNull(httpContext, "httpContext");
        this.methodFactory = checkNotNull(methodFactory, "methodFactory");
    }

    @Override
    public HttpRestResponse post(String url, String contentType, String contents) {

        LOG.trace("POST: " + url);

        HttpPost post = methodFactory.createPost(url, contentType, contents);

        return execute(post);

    }

    @Override
    public HttpRestResponse postForm(String url, List<NameValuePair> params) {
        LOG.trace("POST FORM:" + url + "  PARAMS:" + params);

        HttpPost post = methodFactory.createFormPost(url, params);

        return execute(post);
    }

    @Override
    public HttpRestResponse get(String url) {

        LOG.trace("GET : " + url);

        HttpGet get = methodFactory.createGet(url);

        return execute(get);

    }

    @Override
    public HttpRestResponse put(String url, String contentType, String contents) {

        LOG.trace("PUT : " + url);

        HttpPut put = methodFactory.createPut(url, contentType, contents);

        return execute(put);

    }

    @Override
    public HttpRestResponse delete(String url) {

        LOG.trace("DELETE: " + url);

        HttpDelete delete = methodFactory.createDelete(url);

        return execute(delete);

    }

    @Override
    public void close() {
        client.getConnectionManager().shutdown();
    }

    private HttpRestResponse execute(HttpUriRequest request) {

        LOG.trace("Executing HttpUriRequest: " + request);

        try {

            return new HttpRestResponseImpl(client.execute(request, httpContext));

        } catch (ClientProtocolException e) {
            LOG.error("HTTP protocol error", e);
            request.abort();
            if (e instanceof HttpResponseException) {
                throw new HttpRestClientException("HTTP protocol error", e, ((HttpResponseException) e).getStatusCode());
            } else {
                throw new HttpRestClientException("HTTP protocol error", e);
            }
        } catch (IOException e) {
            LOG.error("Error or connection abort", e);
            request.abort();
            throw new HttpRestClientException("Error or connection abort", e);
        }

    }
}
