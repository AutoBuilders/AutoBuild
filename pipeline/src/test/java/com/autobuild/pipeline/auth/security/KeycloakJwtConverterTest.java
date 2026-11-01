package com.autobuild.pipeline.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakJwtConverterTest {

    private final KeycloakJwtConverter converter = new KeycloakJwtConverter();

    @Test
    void convertReturnsEmptyWhenNoClaimsPresent() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(jwt.getClaim("resource_access")).thenReturn(null);

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void convertExtractsRealmRoles() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("realm_access")).thenReturn(Map.of("roles", List.of("admin", "user")));
        when(jwt.getClaim("resource_access")).thenReturn(null);

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void convertSkipsRealmAccessWithNoRolesKey() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("realm_access")).thenReturn(Map.of("other_key", List.of("something")));
        when(jwt.getClaim("resource_access")).thenReturn(null);

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void convertExtractsClientRoles() {
        Map<String, Object> clientData = Map.of("roles", List.of("read", "write"));
        Map<String, Object> resourceAccess = Map.of("my-client", clientData);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(jwt.getClaim("resource_access")).thenReturn(resourceAccess);

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_READ")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_WRITE")));
    }

    @Test
    void convertSkipsClientEntryWithNoRolesKey() {
        Map<String, Object> clientData = Map.of("other_key", List.of("something"));
        Map<String, Object> resourceAccess = Map.of("my-client", clientData);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(jwt.getClaim("resource_access")).thenReturn(resourceAccess);

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void convertSkipsClientEntryThatIsNotAMap() {
        Map<String, Object> resourceAccess = Map.of("my-client", "not-a-map");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("realm_access")).thenReturn(null);
        when(jwt.getClaim("resource_access")).thenReturn(resourceAccess);

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void convertCombinesRealmAndClientRoles() {
        Map<String, Object> realmAccess = Map.of("roles", List.of("realm-role"));
        Map<String, Object> clientData = Map.of("roles", List.of("client-role"));
        Map<String, Object> resourceAccess = Map.of("app", clientData);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("realm_access")).thenReturn(realmAccess);
        when(jwt.getClaim("resource_access")).thenReturn(resourceAccess);

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_REALM-ROLE")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT-ROLE")));
    }
}
