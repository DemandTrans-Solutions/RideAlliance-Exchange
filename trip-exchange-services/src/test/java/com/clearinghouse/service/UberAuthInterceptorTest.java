package com.clearinghouse.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UberAuthInterceptorTest {

    @Test
    void interceptorAddsBearer_whenTokenPresent() throws IOException {
        UberAuthInterceptor.TokenProvider provider = () -> "abc123";
        UberAuthInterceptor interceptor = new UberAuthInterceptor(provider);

        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(new org.springframework.http.HttpHeaders());

        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);
        when(exec.execute(any(), any())).thenReturn(mock(org.springframework.http.client.ClientHttpResponse.class));

        interceptor.intercept(request, new byte[0], exec);

        assertEquals("Bearer abc123", request.getHeaders().getFirst("Authorization"));
        verify(exec).execute(any(), any());
    }
}
