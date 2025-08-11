package com.saml.server.opensaml.config;

import org.opensaml.core.config.InitializationService;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class OpenSAMLConfig {

    @PostConstruct
    public void init() {
        try {
            InitializationService.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing OpenSAML", e);
        }
    }
}
