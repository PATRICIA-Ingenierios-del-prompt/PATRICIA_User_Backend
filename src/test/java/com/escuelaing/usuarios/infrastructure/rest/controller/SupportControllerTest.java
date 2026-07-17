package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.SupportTicket;
import com.escuelaing.usuarios.domain.model.SupportTicketStatus;
import com.escuelaing.usuarios.domain.port.in.SupportUseCase;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SupportController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, SupportControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class SupportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private SupportUseCase supportUseCase;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtTokenParser jwtTokenParser() {
            return new JwtTokenParser("test-secret-test-secret-test-secret-test-secret");
        }
    }

    private String adminToken() {
        return tokenWithRole("ADMIN");
    }

    private String userToken() {
        return tokenWithRole("STUDENT");
    }

    private String tokenWithRole(String role) {
        SecretKey key = Keys.hmacShaKeyFor(
                "test-secret-test-secret-test-secret-test-secret".getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("roles", List.of(role))
                .signWith(key)
                .compact();
    }

    private SupportTicket sampleTicket() {
        SupportTicket ticket = SupportTicket.create("Karol", "karol@example.com", "Ayuda");
        ticket.setId(UUID.randomUUID());
        return ticket;
    }

    @Test
    void createTicket_withUserToken_returns201() throws Exception {
        SupportTicket ticket = sampleTicket();
        when(supportUseCase.createTicket("Karol", "karol@example.com", "Ayuda")).thenReturn(ticket);

        String body = """
                {"name":"Karol","email":"karol@example.com","message":"Ayuda"}
                """;

        mockMvc.perform(post("/api/v1/usuarios/support")
                        .header("Authorization", userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ticket.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void listTickets_withAdminRole_returns200() throws Exception {
        when(supportUseCase.listAllTickets()).thenReturn(List.of(sampleTicket()));

        mockMvc.perform(get("/api/v1/usuarios/support")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Ayuda"));
    }

    @Test
    void listTickets_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/support"))
                .andExpect(status().isForbidden());
    }

    @Test
    void resolveTicket_withAdminRole_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        SupportTicket ticket = sampleTicket();
        ticket.setId(id);
        ticket.resolve();
        when(supportUseCase.resolveTicket(id)).thenReturn(ticket);

        mockMvc.perform(post("/api/v1/usuarios/support/{id}/resolve", id)
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedAt").exists());
    }
}
