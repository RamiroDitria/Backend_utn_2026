package utnfc.isi.back.taller.commons.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/** Crea el conversor de JWT que usan el gateway y los microservicios. */
public final class KeycloakJwtConverter {

    public static final String CLAIM_USUARIO = "preferred_username";

    private KeycloakJwtConverter() {
    }

    public static JwtAuthenticationConverter crear() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        converter.setPrincipalClaimName(CLAIM_USUARIO);
        return converter;
    }
}
