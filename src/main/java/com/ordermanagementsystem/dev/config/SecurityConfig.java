package com.ordermanagementsystem.dev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/signup", "/login", "/css/**", "/error").permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                // /reports herkese açık (giriş yapmış): customer ve owner farklı rapor görür
                                                .requestMatchers("/customers/**").hasRole("OWNER")
                                                // Ürün listesini herkes görür, ürün ekleme/değiştirme sadece OWNER
                                                .requestMatchers(HttpMethod.POST, "/products/**").hasRole("OWNER")
                                                .requestMatchers("/products/*/edit").hasRole("OWNER")
                                                // Sepet ve satın alma sadece CUSTOMER (owner/admin alışveriş yapmaz)
                                                .requestMatchers("/cart/**").hasRole("CUSTOMER")
                                                // Sipariş iptali / teslim alma: customer kendi siparişi için (sahiplik kontrolü OrderService'te)
                                                .requestMatchers(HttpMethod.POST, "/orders/*/cancel", "/orders/*/received").hasRole("CUSTOMER")
                                                .requestMatchers(HttpMethod.POST, "/orders/**").hasRole("OWNER")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .usernameParameter("userEmail")
                                                .passwordParameter("userPassword")
                                                .defaultSuccessUrl("/", true)
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll());

                return http.build();
        }
}