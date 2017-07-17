package com.asiainfo.comm.externalservice.jenkins.client;

import com.google.inject.ImplementedBy;
import org.apache.http.NameValuePair;

import java.util.List;

@ImplementedBy(HttpRestClientImpl.class)
public interface HttpRestClient extends AutoCloseable {

    HttpRestResponse put(String url, String contentType, String contents);

    HttpRestResponse get(String url);

    HttpRestResponse post(String url, String contentType, String contents);

    HttpRestResponse postForm(String url, List<NameValuePair> params);

    HttpRestResponse delete(String url);

    @Override
    void close();

}
