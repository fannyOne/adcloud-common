package com.asiainfo.cache;

import com.asiainfo.cache.springboot.HttpSessionConfig;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.stereotype.Component;

/**
 * Created by guojian on 01/12/2016.
 */
@Component
public class HttpSessionApplicationInitializer extends AbstractHttpSessionApplicationInitializer {

    public HttpSessionApplicationInitializer() {
        super(HttpSessionConfig.class);
    }
}
