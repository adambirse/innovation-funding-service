package com.worth.ifs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = { "classpath:/application.properties", "classpath:/assessmentapplication.properties" })
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class Application extends SpringBootServletInitializer {
    private static final Log LOG = LogFactory.getLog(Application.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        LOG.info("IFS com.worth.ifs.Application builder configure method");
        return application.sources(Application.class);
    }

    public static void main(String[] args) throws Exception {
        LOG.info("IFS boot com.worth.ifs.Application main method");
        SpringApplication.run(Application.class, args);
    }
}