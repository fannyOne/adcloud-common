package com.asiainfo.comm.externalservice.jenkins.client;

import com.google.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.util.List;

class HttpMethodFactory {

    private final static Log LOG = LogFactory.getLog(HttpMethodFactory.class);

    @Inject
    HttpMethodFactory() {
    }

    HttpPost createFormPost(String url, List<NameValuePair> params) {
        LOG.info("Creating FORM POST: " + url);

        StringEntity entity = createStringEntityForContents(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), "");

        String encoding = ContentType.APPLICATION_FORM_URLENCODED.getCharset().name();
        String completeUrl = url + '?' + URLEncodedUtils.format(params, encoding);

        HttpPost post = new HttpPost(completeUrl);
        post.setEntity(entity);

        return post;
    }

    HttpPost createPost(String url, String contentType, String contents) {
        LOG.info("Creating POST:" + url + "   " + contentType);

        StringEntity entity = createStringEntityForContents(contentType, contents);

        HttpPost post = new HttpPost(url);
        post.setEntity(entity);

        return post;
    }

    HttpGet createGet(String url) {
        LOG.info("Creating GET: " + url);

        return new HttpGet(url);
    }

    HttpPut createPut(String url, String contentType, String contents) {
        LOG.trace("Creating PUT: " + url + "- " + contentType);

        StringEntity entity = createStringEntityForContents(contentType, contents);

        HttpPut put = new HttpPut(url);
        put.setEntity(entity);

        return put;
    }

    HttpDelete createDelete(String url) {
        LOG.trace("Creating DELETE: " + url);

        return new HttpDelete(url);
    }

    private StringEntity createStringEntityForContents(String contentType, String contents) {
        StringEntity entity;
        entity = new StringEntity(contents, "UTF-8");
        entity.setContentType(contentType);

        return entity;
    }
}
