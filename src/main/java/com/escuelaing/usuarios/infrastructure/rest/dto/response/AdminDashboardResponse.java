package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import com.escuelaing.usuarios.domain.model.AdminDashboard;

import java.util.List;

public record AdminDashboardResponse(
        long totalUsuarios,
        List<CarreraCountResponse> carreraBreakdown,
        List<RecentSignupResponse> recentSignups
) {
    public static AdminDashboardResponse from(AdminDashboard dashboard) {
        return new AdminDashboardResponse(
                dashboard.getTotalUsuarios(),
                dashboard.getCarreraBreakdown().stream()
                        .map(c -> new CarreraCountResponse(c.getCarrera(), c.getCount()))
                        .toList(),
                dashboard.getRecentSignups().stream()
                        .map(s -> new RecentSignupResponse(s.getId(), s.getName(), s.getCarrera(), s.getDate()))
                        .toList()
        );
    }

    public record CarreraCountResponse(String carrera, long count) {
    }

    public record RecentSignupResponse(String id, String name, String carrera, String date) {
    }
}
