package com.algorycode.rent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
  @ConditionalOnProperty(prefix = "app.cors", name = "enabled", havingValue = "true", matchIfMissing = false)
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origin-patterns:*}") String allowedOriginPatterns) {
    String[] patterns =
        Arrays.stream(allowedOriginPatterns.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
    if (patterns.length == 0) {
      patterns = new String[] {"*"};
    }

    boolean allowAnyOrigin = patterns.length == 1 && "*".equals(patterns[0]);

    CorsConfiguration config = new CorsConfiguration();
    // "*" ile credential gönderilemez; aksi halde FE withCredentials için true
    config.setAllowCredentials(!allowAnyOrigin);
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
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, @Value("${app.cors.enabled:false}") boolean corsEnabled) throws Exception {
    if (corsEnabled) {
      http.cors(Customizer.withDefaults());
    } else {
      http.cors(AbstractHttpConfigurer::disable);
    }
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }
}
