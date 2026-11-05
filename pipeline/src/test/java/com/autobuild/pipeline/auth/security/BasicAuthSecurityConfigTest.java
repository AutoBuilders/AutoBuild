package com.autobuild.pipeline.auth.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class BasicAuthSecurityConfigTest {
    @Mock
    private HttpSecurity mockHttpSecurity;

    @InjectMocks
    private BasicAuthSecurityConfig basicAuthSecurityConfig;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        doReturn(mockHttpSecurity).when(mockHttpSecurity).csrf(any());
        doReturn(mockHttpSecurity).when(mockHttpSecurity).cors(any());
        doReturn(mockHttpSecurity).when(mockHttpSecurity).authorizeHttpRequests(any());
        doReturn(mockHttpSecurity).when(mockHttpSecurity).httpBasic(any());
        doReturn(mockHttpSecurity).when(mockHttpSecurity).formLogin(any());
    }

    @Test
    void testFilterChain() throws Exception {
        basicAuthSecurityConfig.filterChain(mockHttpSecurity);

        verify(mockHttpSecurity,times(1)).csrf(any());
        verify(mockHttpSecurity,times(1)).authorizeHttpRequests(any());
        verify(mockHttpSecurity,times(1)).httpBasic(any());
        verify(mockHttpSecurity,times(1)).formLogin(any());
    }
}
