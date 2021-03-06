package com.github.platform.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by serv on 2016/10/18.
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 此登录页仅做测试用
        registry.addViewController("/login").setViewName("login");

        registry.addRedirectViewController("/api.html", "/swagger-ui.html");
        registry.addRedirectViewController("/api", "/swagger-ui.html");
    }


    @Bean
    @ConditionalOnMissingBean
    public GlobResponseBodyAdviceAdapter globRequestBodyAdviceAdapter() {
        return new GlobResponseBodyAdviceAdapter();
    }


    @Bean
    public ApplicationPidFileWriter applicationPidFileWriter() {
        return new ApplicationPidFileWriter();
    }

}
