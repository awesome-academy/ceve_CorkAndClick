package com.sun.wineshop.configuration;

import com.sun.wineshop.model.enums.UserRole;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
import com.sun.wineshop.utils.api.CategoryApiPaths;
import com.sun.wineshop.utils.api.ProductApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import static com.sun.wineshop.utils.api.AuthApiPaths.Endpoint.*;
import static com.sun.wineshop.utils.api.UserApiPaths.Endpoint.FULL_REGISTER;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ROLE = "ROLE_";

    private final String[] PUBLIC_ENDPOINTS = {FULL_REGISTER, FULL_LOGIN, FULL_LOGOUT, FULL_VERIFY_TOKEN,
            ProductApiPaths.BASE_ALL,
            CategoryApiPaths.BASE};

    private final MessageUtil messageUtil;
    private final CustomJwtDecoder jwtDecoder;

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint(messageUtil);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(messageUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests ->
                requests
                        // add end points with not auth here
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // add end points for admin here
                        .requestMatchers(AdminApiPaths.BASE_ALL)
                        .hasRole(UserRole.ADMIN.name())
                        .anyRequest().authenticated());
        http.oauth2ResourceServer(oAuth2 ->
                oAuth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(authenticationEntryPoint()));
        http.csrf(AbstractHttpConfigurer::disable);
        http.exceptionHandling(exception -> {
            exception.accessDeniedHandler(accessDeniedHandler());
        });
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(ROLE);
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
