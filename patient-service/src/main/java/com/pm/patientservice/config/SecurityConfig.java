package com.pm.patientservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF so POST requests don't require a token
                .csrf(csrf -> csrf.disable())

                // 2. Explicitly permit the patients endpoint and H2
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/patients/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/v3/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 3. Fix H2 Console display issues
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 4. Standard login for everything else
                .formLogin(form -> form.permitAll());

        return http.build();
    }
}
