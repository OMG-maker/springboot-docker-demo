package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/hello").permitAll()
                .anyRequest().authenticated()
            )
            // .httpBasic(httpBasic -> httpBasic.realmName("MyApp"));
            .httpBasic(httpBasic -> httpBasic
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setHeader("WWW-Authenticate", "Basic realm=\"MyApp\"");
                        response.getWriter().write("Unauthorized");
                    })
                );
        return http.build();
    }
}