package com.escuelaing.usuarios.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Cliente S3 para subir fotos de perfil del onboarding. Usa la cadena de
 * credenciales por defecto del SDK (variables de entorno, perfil ~/.aws,
 * o rol de la instancia/task cuando corra en AWS), así que no requiere
 * configuración adicional una vez desplegado con las variables de entorno
 * AWS_S3_BUCKET / AWS_S3_REGION (ver application.yml).
 */
@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(@Value("${aws.s3.region}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
