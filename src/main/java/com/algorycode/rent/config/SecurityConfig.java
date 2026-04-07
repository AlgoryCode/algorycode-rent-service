package com.algorycode.rent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
          String allowedOriginPatterns) {
    String[] patterns =
        Arrays.stream(allowedOriginPatterns.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
    if (patterns.length == 0) {
      patterns = new String[] {"http://localhost:*"};
    }

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOriginPatterns(Arrays.asList(patterns));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  /**
   * Kiracı JWT / oturum eklenene kadar API tamamen açık; CORS burada tanımlı.
   * İleride {@code authorizeHttpRequests} ile kısıtlayın.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }
}
