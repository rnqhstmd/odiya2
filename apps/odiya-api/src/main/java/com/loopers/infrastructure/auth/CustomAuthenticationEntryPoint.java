package com.loopers.infrastructure.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.security.JwtAuthenticationFilter;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        ErrorType errorType = (ErrorType) request.getAttribute(JwtAuthenticationFilter.ERROR_TYPE_ATTRIBUTE);
        if (errorType == null) {
            errorType = ErrorType.UNAUTHORIZED;
        }

        ApiResponse<Object> apiResponse = ApiResponse.fail(errorType.getCode(), errorType.getMessage());

        response.setStatus(errorType.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
