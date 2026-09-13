package com.vegetablemarket.config;

import com.vegetablemarket.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/payment.html", "/favicon.ico").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/products/seller/my-products").hasRole("SELLER")
                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/products/*").hasRole("SELLER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/*").hasRole("SELLER")
                .requestMatchers(HttpMethod.GET, "/api/seller/orders", "/api/seller/orders/*").hasRole("SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/seller/orders/*/status").hasRole("SELLER")
                .requestMatchers(HttpMethod.GET, "/api/seller/dashboard").hasRole("SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/products/*/inventory").hasRole("SELLER")

                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*").hasAnyRole("CUSTOMER", "SELLER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
