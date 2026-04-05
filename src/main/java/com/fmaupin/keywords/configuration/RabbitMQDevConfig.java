/*
 * Copyright (C) 2025 Fabrice MAUPIN
 *
 * This file is part of Extract Micro Service.
 *
 * Extract Micro Service is free software: you can redistribute it and/or modify
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

import java.util.Objects;
import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQDevConfig
 *
 * Configuration pour RabbitMQ (mode dev).
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 29/10/25
 */
@EnableRabbit
@Configuration
@Profile("dev")
@Slf4j
public class RabbitMQDevConfig {

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private Integer port;

    @Value("${spring.rabbitmq.virtual-host}")
    private String vhost;

    @Value("${spring.rabbitmq.ssl.enabled:false}")
    private boolean sslEnabled;

    // utile pour le listener
    @Value("${keywords-poc.rabbitmq.in.consumerQueueName}")
    private String inQueueName;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);

        connectionFactory
                .setVirtualHost(Objects.requireNonNull(vhost, "RabbitMQDevConfig virtual host must not be null"));
        connectionFactory.setUsername(Objects.requireNonNull(username, "RabbitMQDevConfig username must not be null"));
        connectionFactory.setPassword(Objects.requireNonNull(password, "RabbitMQDevConfig password must not be null"));

        if (sslEnabled) {
            log.warn("RabbitMQConfig - SSL is enabled but ignored in dev profile");
        }

        // pas de TLS ni de checks stricts
        connectionFactory.setConnectionNameStrategy(cf -> "extract-dev" + UUID.randomUUID());

        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        // Désactive la déclaration automatique
        admin.setAutoStartup(false);

        return admin;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // pouvant être requis par d'autres composants
    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());

        return template;
    }

    @PostConstruct
    public void validateConfig() {
        if (inQueueName == null || inQueueName.isBlank()) {
            throw new IllegalArgumentException("RabbitMQDevConfig - consumerQueueName must be provided");
        }

        log.info("RabbitMQDevConfig initialized for queue '{}'", inQueueName);
    }

}
