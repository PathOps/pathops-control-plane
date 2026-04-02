package io.pathops.controlplane.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class JWTSecurityConfig {
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http.csrf(csrf -> csrf.disable())
	    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	    .authorizeHttpRequests(authz -> authz
		    .requestMatchers("/actuator/health/**").permitAll()
		    .requestMatchers("/actuator/info").permitAll()
		    .requestMatchers("/actuator/**").denyAll()
	    	.requestMatchers("/public/login").authenticated()
	    	.anyRequest().authenticated());
	    return http.build();
	}
}
