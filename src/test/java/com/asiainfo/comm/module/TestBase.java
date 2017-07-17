package com.asiainfo.comm.module;

import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestBase.TestConfiguration.class)
@ActiveProfiles("cloudqa")
public class TestBase {

    @EnableAutoConfiguration
    @Configuration
    @ComponentScan(basePackages = "com.asiainfo")
    static class TestConfiguration {
    }
}
