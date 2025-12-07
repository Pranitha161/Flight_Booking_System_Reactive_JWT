package com.flightapp.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.flightapp.demo.filter.JwtAuthFilter;

@Configuration
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(exchanges -> exchanges.pathMatchers("/auth/signup", "/auth/login").permitAll()
						.pathMatchers("/api/booking/**").hasAuthority("ROLE_USER").anyExchange().authenticated())
				// Add your reactive JWT filter here
				.addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION).build();
	}
}
