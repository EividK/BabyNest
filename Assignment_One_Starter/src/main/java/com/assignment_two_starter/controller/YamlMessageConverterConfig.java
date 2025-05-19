package com.assignment_two_starter.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@Configuration
public class YamlMessageConverterConfig {

    @Bean
    public HttpMessageConverter<Object> yamlHttpMessageConverter() {
        YAMLMapper yamlMapper = new YAMLMapper();
        return new AbstractJackson2HttpMessageConverter(yamlMapper, MediaType.valueOf("application/x-yaml")) {};
    }
}
