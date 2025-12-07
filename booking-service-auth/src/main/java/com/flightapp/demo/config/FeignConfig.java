package com.flightapp.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import feign.RequestInterceptor;

@Configuration
public class FeignConfig {
	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication instanceof UsernamePasswordAuthenticationToken) {
				String jwt = (String) authentication.getCredentials();
				if (jwt != null) {
					requestTemplate.header("Authorization", "Bearer " + jwt);
				}
			}
		};
	}
}
