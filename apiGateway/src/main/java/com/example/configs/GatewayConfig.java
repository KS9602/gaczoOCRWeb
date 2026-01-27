package com.example.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${storage.url}")
    private String storageUrl;

    @Value("${storage.paths}")
    private String storagePaths;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("invoice-upload", r -> r
                        .path("/invoice")
                        .filters(f -> f.setPath("/uploads/invoice"))
                        .uri(storageUrl)
                )
                .route("zip-download", r -> r
                        .path("/zip/**")
                        .filters(f -> f.rewritePath("/zip/(?<segment>.*)", "/downloads/zip/${segment}"))
                        .uri(storageUrl)
                )
                .route("storage-generic", r -> r
                        .path("/storage/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(storageUrl)
                )
                .build();
    }
}