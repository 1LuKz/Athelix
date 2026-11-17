package br.com.athletix.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AthletixMailProperties.class)
public class AthletixMailConfiguration {}
