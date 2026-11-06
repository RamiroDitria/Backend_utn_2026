package utnfc.isi.back.taller.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import utnfc.isi.back.taller.commons.security.KeycloakJwtConverter;

/**
 * Primer control de seguridad: valida el JWT y aplica permisos gruesos por ruta.
 * Los permisos finos (qué rol puede cada operación) los vuelve a validar cada servicio.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String JEFE = "JEFE_TALLER";
    private static final String MECANICO = "MECANICO";

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex
                .pathMatchers("/api/notificaciones/**").hasRole(JEFE)
                .pathMatchers(HttpMethod.GET, "/api/**").hasAnyRole(JEFE, MECANICO)
                .pathMatchers("/api/clientes/**", "/api/vehiculos/**").hasRole(JEFE)
                .pathMatchers("/api/ordenes/**").hasAnyRole(JEFE, MECANICO)
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                        new ReactiveJwtAuthenticationConverterAdapter(KeycloakJwtConverter.crear()))));
        return http.build();
    }
}
