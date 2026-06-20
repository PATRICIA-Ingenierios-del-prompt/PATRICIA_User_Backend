package com.escuelaing.usuarios.infrastructure.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.escuelaing.usuarios.infrastructure.client")
public class FeignConfig {
}
