package com.flightapp.demo.config;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import feign.RequestInterceptor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

@Configuration
public class FeignConfig {

	@Bean
	public HttpMessageConverters httpMessageConverters() {
		return new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
	}
}
