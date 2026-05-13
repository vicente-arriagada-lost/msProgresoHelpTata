package com.Progreso.ms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

//* Configuración de seguridad para desarrollo
//! En producción, reemplazar por validación de JWT con el OAuth2 Resource Server
//! que ya está disponible como dependencia en el pom.xml
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            //* Desactiva CSRF porque la API es stateless (REST + JSON)
            .csrf(csrf -> csrf.disable())
            //* Permite todas las solicitudes sin autenticación (modo desarrollo)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

}
