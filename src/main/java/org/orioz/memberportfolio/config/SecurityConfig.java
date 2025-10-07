package org.orioz.memberportfolio.config;

import org.orioz.memberportfolio.auth.jwt.JwtAuthenticationWebFilter;
import org.orioz.memberportfolio.auth.properties.SecurityMethod;
import org.orioz.memberportfolio.auth.properties.SecurityProperties;
import org.orioz.memberportfolio.auth.properties.SecurityRule;
import org.orioz.memberportfolio.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;
    private final SecurityProperties securityProperties;

    @Autowired
    public SecurityConfig(
            JwtAuthenticationWebFilter jwtAuthenticationWebFilter,
            SecurityProperties securityProperties
    ) {
        this.jwtAuthenticationWebFilter = jwtAuthenticationWebFilter;
        this.securityProperties = securityProperties;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.cors(cors -> {});

        http.authorizeExchange(exchanges -> {
            for (SecurityRule rule : securityProperties.getRules()) {
                String path = rule.getPath();
                for (SecurityMethod method : rule.getMethods()) {
                    String methodName = method.getName();
                    List<String> roles = method.getRoles();

                    if ("ALL".equalsIgnoreCase(methodName)) {
                        if (roles.contains("ANONYMOUS")) {
                            exchanges.pathMatchers(path).permitAll();
                        } else {
                            exchanges.pathMatchers(path).hasAnyAuthority(roles.toArray(new String[0]));
                        }
                    } else {
                        if (roles.contains("ANONYMOUS")) {
                            exchanges.pathMatchers(org.springframework.http.HttpMethod.valueOf(methodName), path).permitAll();
                        } else {
                            exchanges.pathMatchers(org.springframework.http.HttpMethod.valueOf(methodName), path).hasAnyAuthority(roles.toArray(new String[0]));
                        }
                    }
                }
            }
            exchanges.anyExchange().authenticated();
        });

        http.addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
