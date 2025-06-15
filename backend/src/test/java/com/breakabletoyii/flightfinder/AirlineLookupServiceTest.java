package com.breakabletoyii.flightfinder;

import com.breakabletoyii.flightfinder.amadeus.AirlineLookupService;
import com.breakabletoyii.flightfinder.amadeus.AuthService;
import com.breakabletoyii.flightfinder.amadeus.HttpService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AirlineLookupServiceTest {

    private AuthService authServiceMock;
    private HttpService httpServiceMock;
    private AirlineLookupService airlineLookupService;

    @BeforeEach
    void setUp() {
        authServiceMock = mock(AuthService.class);
        httpServiceMock = mock(HttpService.class);
        airlineLookupService = new AirlineLookupService(authServiceMock, httpServiceMock);
    }

    @Test
    void shouldReturnAirlineNameFromCache() throws Exception {

        Field cacheField = AirlineLookupService.class.getDeclaredField("airlineNameCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> cache = (Map<String, String>) cacheField.get(airlineLookupService);
        cache.put("AA", "American Airlines");


        String result = airlineLookupService.getAirlineName("AA");

        // Verify result
        assertEquals("American Airlines", result);

        //cache
        verify(authServiceMock, never()).getAccessToken();
        verify(httpServiceMock, never()).sendGet(anyString(), anyString());
    }
}
