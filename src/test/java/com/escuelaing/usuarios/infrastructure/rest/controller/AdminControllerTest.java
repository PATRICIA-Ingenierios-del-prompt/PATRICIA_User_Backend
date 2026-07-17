package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.AdminDashboard;
import com.escuelaing.usuarios.domain.port.in.AdminDashboardUseCase;
import com.escuelaing.usuarios.infrastructure.config.SecurityConfig;
import com.escuelaing.usuarios.infrastructure.rest.advice.GlobalExceptionHandler;
import com.escuelaing.usuarios.infrastructure.security.JwtTokenParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AdminControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AdminDashboardUseCase adminDashboardUseCase;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtTokenParser jwtTokenParser() {
            return new JwtTokenParser("test-secret-test-secret-test-secret-test-secret");
        }
    }

    private String adminToken() {
        SecretKey key = Keys.hmacShaKeyFor(
                "test-secret-test-secret-test-secret-test-secret".getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("roles", List.of("ADMIN"))
                .signWith(key)
                .compact();
    }

    @Test
    void getDashboard_withAdminRole_returns200() throws Exception {
        when(adminDashboardUseCase.getDashboard()).thenReturn(
                AdminDashboard.builder()
                        .totalUsuarios(10L)
                        .carreraBreakdown(List.of(new AdminDashboard.CarreraCount("Sistemas", 5L)))
                        .recentSignups(List.of(new AdminDashboard.RecentSignup(UUID.randomUUID().toString(), "Karol", "Sistemas", "2026-07-17T00:00:00Z")))
                        .build()
        );

        mockMvc.perform(get("/api/v1/usuarios/admin/dashboard")
                        .header("Authorization", adminToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").value(10))
                .andExpect(jsonPath("$.carreraBreakdown[0].carrera").value("Sistemas"))
                .andExpect(jsonPath("$.recentSignups[0].name").value("Karol"));
    }

    @Test
    void getDashboard_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/admin/dashboard"))
                .andExpect(status().isForbidden());
    }
}
