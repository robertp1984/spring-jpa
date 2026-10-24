package org.softwarecave.springjpa.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(getAuthorizationManagerRequestMatcherRegistryCustomizer())
                .oauth2ResourceServer(config -> config.jwt(Customizer.withDefaults()));
        return http.build();
    }

    private Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>
            getAuthorizationManagerRequestMatcherRegistryCustomizer() {
        return auth -> auth.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator", "/actuator/**")
                .hasAnyAuthority(Role.ACTUATOR_READ.title())
                .requestMatchers("/actuator", "/actuator/**")
                .hasAnyAuthority(Role.ACTUATOR_WRITE.title())
                .requestMatchers(HttpMethod.GET, "/api/v1/assets", "/api/v1/assets/**")
                .hasAnyAuthority(Role.ASSET_READ.title())
                .requestMatchers("/api/v1/assets", "/api/v1/assets/**")
                .hasAnyAuthority(Role.ASSET_WRITE.title())
                .requestMatchers(HttpMethod.GET, "/api/v1/assetClasses", "/api/v1/assetClasses/**")
                .hasAnyAuthority(Role.ASSET_CLASS_READ.title())
                .requestMatchers("/api/v1/assetClasses", "/api/v1/assetClasses/**")
                .hasAnyAuthority(Role.ASSET_CLASS_WRITE.title())
                .anyRequest()
                .denyAll();
    }
}
