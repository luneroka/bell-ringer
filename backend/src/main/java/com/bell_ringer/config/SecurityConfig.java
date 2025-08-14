package com.bell_ringer.config;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class SecurityConfig {

  private final FirebaseAuth firebaseAuth;

  public SecurityConfig(FirebaseAuth firebaseAuth) {
    this.firebaseAuth = firebaseAuth;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    var firebaseFilter = new FirebaseAuthenticationFilter(firebaseAuth);

    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(request -> {
          var c = new CorsConfiguration();
          // Adjust for your frontend origin(s)
          c.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
          c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
          c.setAllowedHeaders(List.of("*"));
          c.setAllowCredentials(true);
          return c;
        }))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers("/actuator/health", "/public/**").permitAll()
            // Everything else requires authentication
            .anyRequest().authenticated()
        )
        // Insert our Firebase filter before the anonymous auth filter
        .addFilterBefore(firebaseFilter, AnonymousAuthenticationFilter.class)
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }
}