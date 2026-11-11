package com.parking.security;
import com.parking.repo.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@Configuration
@EnableMethodSecurity   // habilita @PreAuthorize (RF-04)
public class SecurityConfig {

  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  /** RF-02, RF-03, RF-06: usuarios inactivos quedan deshabilitados y no pueden autenticarse. */
  @Bean UserDetailsService userDetailsService(UserRepository repo) {
    return username -> repo.findByUsername(username)
        .map(u -> org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
            .password(u.getPasswordHash())
            .roles(u.getRole().name())     // ROLE_ADMIN / ROLE_OPERATOR
            .disabled(!u.isActive())
            .build())
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
  }

  @Bean SecurityFilterChain chain(HttpSecurity http) throws Exception {
    http.csrf(c -> c.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a
            .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
            .requestMatchers("/", "/index.html", "/app.js").permitAll()
            .requestMatchers("/api/users/**", "/api/tariffs/**").hasRole("ADMIN")
            .requestMatchers("/api/transactions/**").hasAnyRole("ADMIN", "OPERATOR")
            .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "OPERATOR")
            .requestMatchers("/api/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults());   // En produccion: reemplazar por JWT
    return http.build();
  }

  @Bean CorsConfigurationSource cors() {
    CorsConfiguration c = new CorsConfiguration();
    c.addAllowedOriginPattern("*"); c.addAllowedHeader("*"); c.addAllowedMethod("*");
    UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
    s.registerCorsConfiguration("/**", c);
    return s;
  }
}
