/*
*  @(#)WebCorsConfig.java
*
*  Copyright (c) J-Tech Solucoes em Informatica.
*  All Rights Reserved.
*
*  This software is the confidential and proprietary information of J-Tech.
*  ("Confidential Information"). You shall not disclose such Confidential
*  Information and shall use it only in accordance with the terms of the
*  license agreement you entered into with J-Tech.
*
*/
package br.com.jtech.tasklist.config.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing) para permitir
 * requisições de diferentes origens.
 *
 * @author JTech
 */
@Configuration
public class WebCorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Configura origens permitidas
        if ("*".equals(allowedOrigins)) {
            config.addAllowedOriginPattern("*");
        } else {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            origins.forEach(origin -> {
                if (origin.trim().startsWith("*")) {
                    config.addAllowedOriginPattern(origin.trim());
                } else {
                    config.addAllowedOrigin(origin.trim());
                }
            });
        }

        // Configura métodos HTTP permitidos
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        methods.forEach(method -> config.addAllowedMethod(method.trim()));

        // Configura headers permitidos
        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            headers.forEach(header -> config.addAllowedHeader(header.trim()));
        }

        // Permite credenciais (cookies, authorization headers, etc.)
        config.setAllowCredentials(allowCredentials);

        // Tempo de cache do preflight request
        config.setMaxAge(maxAge);

        // Headers expostos na resposta
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count",
            "Location"
        ));

        // Aplica a configuração para todos os endpoints
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

