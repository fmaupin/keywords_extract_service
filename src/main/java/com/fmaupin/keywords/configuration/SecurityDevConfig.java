/*
 * Copyright (C) 2025 Fabrice MAUPIN
 *
 * This file is part of Extract Micro Service.
 *
 * Read Content Micro Service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.fmaupin.keywords.configuration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * SecurityDevConfig
 *
 * Configuration pour spring security.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 05/12/25
 */
@Configuration
@Profile("dev")
@EnableWebSecurity
@Slf4j
public class SecurityDevConfig {

    private static final String ROLE_ADMIN = "ADMIN";

    private static final String ROLE_DEV = "DEV";

    @Value("${app.security.username}")
    private String username;

    @Value("${app.security.password}")
    private String password;

    @Value("${app.security.roles}")
    private String[] roles;

    @PostConstruct
    void init() {
        log.info("SecurityConfig [dev] active - {} roles - Actuator protected with HTTP Basic.",
                Arrays.toString(roles));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").hasAnyRole(ROLE_DEV, ROLE_ADMIN)
                        .requestMatchers("/actuator/metrics").hasAnyRole(ROLE_DEV, ROLE_ADMIN)
                        .requestMatchers("/actuator/**").hasRole(ROLE_ADMIN)
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        log.info("Creating in-memory user '{}' with roles {}", username, Arrays.toString(roles));

        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password("{noop}" + password) // pas d’encodage pour dev
                        .roles(roles)
                        .build());
    }
}
