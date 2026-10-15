package com.ordermanagementsystem.dev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// Geliştirme (dev) profilinde tüm isteklere giriş yapmadan izin verir.
// JWT eklendiğinde bu yapılandırma değiştirilmeli.
@Configuration
@Profile("dev")
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}

}
