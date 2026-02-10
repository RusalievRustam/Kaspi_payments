//package com.kaspi.payment.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SimpleSecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())  // Для API обычно отключаем
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/v1/payments/health").permitAll()
//                        .requestMatchers("/api/v1/payments/**").authenticated()
//                        .anyRequest().permitAll()
//                );
//
//        return http.build();
//    }
//}