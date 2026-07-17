package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.AdminDashboard;
import com.escuelaing.usuarios.domain.port.in.AdminDashboardUseCase;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.AdminDashboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios/admin")
public class AdminController {

    private final AdminDashboardUseCase adminDashboardUseCase;

    public AdminController(AdminDashboardUseCase adminDashboardUseCase) {
        this.adminDashboardUseCase = adminDashboardUseCase;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        AdminDashboard dashboard = adminDashboardUseCase.getDashboard();
        return ResponseEntity.ok(AdminDashboardResponse.from(dashboard));
    }
}
