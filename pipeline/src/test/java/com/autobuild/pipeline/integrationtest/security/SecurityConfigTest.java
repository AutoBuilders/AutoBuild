package com.autobuild.pipeline.integrationtest.security;

import com.autobuild.pipeline.configuration.FeatureFlagService;
import com.autobuild.pipeline.definiton.repository.PipelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for SecurityConfig with Keycloak JWT authentication.
 * Tests profiles where @Profile("!default & !basicAuth & !test") is active (e.g., demo, prod).
 * Verifies that Keycloak JWT authentication is enforced.
 * 
 * @author Baibhab Dey
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@TestPropertySource(properties = {
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://mock-issuer.test",
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://mock-jwk-set-uri.test",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PipelineRepository pipelineRepository;

    @MockitoBean
    private FeatureFlagService featureFlagService;

    @BeforeEach
    void setUp() {
        // Mock pipeline repository to return empty list
        when(pipelineRepository.findAll()).thenReturn(Collections.emptyList());
    }
    
    @Test
    void testProtectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/pipeline"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpointWithValidJwtReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/pipeline")
                .with(jwt()
                    .jwt(builder -> builder
                        .claim("preferred_username", "testuser")
                        .claim("email", "test@example.com")
                        .claim("sub", "test-user-id")
                    )
                ))
                .andExpect(status().isOk());
    }
    
    @Test
    void testJwtWithRealmRolesIsAccepted() throws Exception {
        mockMvc.perform(get("/api/v1/pipeline")
                .with(jwt()
                    .jwt(builder -> builder
                        .claim("preferred_username", "admin")
                        .claim("email", "admin@example.com")
                        .claim("sub", "admin-123")
                        .claim("realm_access", java.util.Map.of(
                            "roles", java.util.List.of("PIPELINE_ADMIN", "USER")
                        ))
                    )
                ))
                .andExpect(status().isOk());
    }

    @Test
    void testJwtWithClientRolesIsAccepted() throws Exception {
        mockMvc.perform(get("/api/v1/pipeline")
                .with(jwt()
                    .jwt(builder -> builder
                        .claim("preferred_username", "client-user")
                        .claim("email", "client@example.com")
                        .claim("sub", "client-123")
                        .claim("resource_access", java.util.Map.of(
                            "pipeline-service", java.util.Map.of(
                                "roles", java.util.List.of("pipeline-creator", "pipeline-viewer")
                            )
                        ))
                    )
                ))
                .andExpect(status().isOk());
    }

    @Test
    void testJwtWithBothRealmAndClientRoles() throws Exception {
        mockMvc.perform(get("/api/v1/pipeline")
                .with(jwt()
                    .jwt(builder -> builder
                        .claim("preferred_username", "poweruser")
                        .claim("email", "power@example.com")
                        .claim("sub", "power-123")
                        .claim("realm_access", java.util.Map.of(
                            "roles", java.util.List.of("USER")
                        ))
                        .claim("resource_access", java.util.Map.of(
                            "pipeline-service", java.util.Map.of(
                                "roles", java.util.List.of("pipeline-admin")
                            )
                        ))
                    )
                ))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testWithMockUserAnnotation() throws Exception {
        mockMvc.perform(get("/api/v1/pipeline"))
                .andExpect(status().isOk());
    }
    
    @Test
    void testJwtWithMissingClaimsStillAuthenticates() throws Exception {
        mockMvc.perform(get("/api/v1/pipeline")
                .with(jwt()
                    .jwt(builder -> builder
                        .claim("sub", "minimal-user")
                    )
                ))
                .andExpect(status().isOk());
    }

    @Test
    void testJwtWithEmptyRoles() throws Exception {
        mockMvc.perform(get("/api/v1/pipeline")
                .with(jwt()
                    .jwt(builder -> builder
                        .claim("preferred_username", "noroles")
                        .claim("email", "noroles@example.com")
                        .claim("sub", "noroles-123")
                        .claim("realm_access", java.util.Map.of(
                            "roles", java.util.List.of()
                        ))
                    )
                ))
                .andExpect(status().isOk());
    }
}