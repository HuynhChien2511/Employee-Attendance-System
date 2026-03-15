/*
 * FILE: WebConfig.java
 * PURPOSE: Spring MVC configuration class that registers application-level web
 *          infrastructure. Currently it wires AuthInterceptor to all /api/** routes.
 *
 * METHODS:
 *  - addInterceptors(registry)
 *      Registers AuthInterceptor for /api/** so all API calls require authentication
 *      unless explicitly skipped inside the interceptor itself.
 *
 * HOW TO MODIFY:
 *  - To exclude specific API paths from interception: add .excludePathPatterns(...)
 *    when registering the interceptor.
 *  - To add more interceptors (logging, metrics, locale): register them here in
 *    the desired execution order.
 *  - To add CORS rules globally: implement addCorsMappings() in this class.
 */
package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
    }
}
