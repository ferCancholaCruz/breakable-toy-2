package com.breakabletoyii.flightfinder;

import com.breakabletoyii.flightfinder.amadeus.AuthService;
import com.breakabletoyii.flightfinder.config.AmadeusApiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AmadeusApiConfig configMock;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        configMock = mock(AmadeusApiConfig.class);
        authService = new AuthService(configMock);
    }

    @Test
    void shouldReturnCachedAccessTokenIfStillValid() throws Exception {

        Field tokenField = AuthService.class.getDeclaredField("accessToken");
        Field expirationField = AuthService.class.getDeclaredField("tokenExpiration");
        tokenField.setAccessible(true);
        expirationField.setAccessible(true);

        tokenField.set(authService, "mocked-token");
        expirationField.set(authService, System.currentTimeMillis() + 60000);

        String token = authService.getAccessToken();

        assertEquals("mocked-token", token);
        verify(configMock, never()).getKey();
        verify(configMock, never()).getSecret();
    }
}
