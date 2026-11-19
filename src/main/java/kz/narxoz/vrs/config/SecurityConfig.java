package kz.narxoz.vrs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import kz.narxoz.vrs.handler.ApiError;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

// правила доступа. Порядок как в таблице README: первое совпадение побеждает
@Configuration
@EnableMethodSecurity
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SecurityConfig {

    private final ObjectMapper json;

    public SecurityConfig(ObjectMapper json) {
        this.json = json;
    }

    @Bean
    public SecurityFilterChain api(HttpSecurity http) throws Exception {
        // 401: нет токена, он испорчен, чужая подпись, истёк или чужой iss
        AuthenticationEntryPoint noToken = (request, response, ex) ->
                writeJson(response, 401, new ApiError("UNAUTHENTICATED", "valid token required",
                        ApiError.correlationId(request)));
        // 403: токен хороший, но роль не та
        AccessDeniedHandler wrongRole = (request, response, ex) ->
                writeJson(response, 403, new ApiError("FORBIDDEN", "not allowed for your role",
                        ApiError.correlationId(request)));

        return http
                .authorizeHttpRequests(rules -> rules
                        .requestMatchers(POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(GET, "/actuator/health").permitAll()
                        .requestMatchers(GET, "/", "/index.html", "/style.css", "/app.js", "/favicon.ico").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // узкое правило выше широкого, иначе сотрудник смог бы согласовать
                        .requestMatchers(POST, "/api/v1/vehicle-requests/*/approve").hasRole("MANAGER")
                        .requestMatchers("/api/v1/**").hasAnyRole("EMPLOYEE", "MANAGER")
                        .requestMatchers(GET, "/api/me").hasAnyRole("EMPLOYEE", "MANAGER")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(rolesFromToken()))
                        .authenticationEntryPoint(noToken)
                        .accessDeniedHandler(wrongRole))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(noToken)
                        .accessDeniedHandler(wrongRole))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    // роли лежат в claim "roles": ["MANAGER"]; hasRole("MANAGER") ищет ROLE_MANAGER
    private JwtAuthenticationConverter rolesFromToken() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    // 401 и 403 случаются в фильтрах, до контроллера, поэтому ApiErrorHandler их не видит
    private void writeJson(HttpServletResponse response, int status, ApiError body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getOutputStream(), body);
    }
}
