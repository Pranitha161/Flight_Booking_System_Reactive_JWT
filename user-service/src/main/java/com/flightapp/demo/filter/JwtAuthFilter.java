package com.flightapp.demo.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.flightapp.demo.util.JwtUtil;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements WebFilter {

	private final JwtUtil jwtUtil;

	public JwtAuthFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				String username = jwtUtil.extractUsername(token);
				if (username != null && jwtUtil.validateToken(token)) {
					List<GrantedAuthority> authorities = jwtUtil.getAuthorities(token);
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username,
							token, authorities);
					return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder
							.withSecurityContext(Mono.just(new SecurityContextImpl(authToken))));
				}
			} catch (Exception e) {
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}
		}
		return chain.filter(exchange);
	}
}
